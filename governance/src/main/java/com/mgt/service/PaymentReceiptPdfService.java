package com.mgt.service;

import java.io.ByteArrayOutputStream;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mgt.model.PaymentReceipt;
import com.mgt.model.PaymentTransaction;

@Service
public class PaymentReceiptPdfService {

    private static final DeviceRgb GREEN_DARK = new DeviceRgb(0x05, 0x4a, 0x35);
    private static final DeviceRgb GREEN_HEADER = new DeviceRgb(0x00, 0x5a, 0x40);
    private static final DeviceRgb GREEN_LIGHT = new DeviceRgb(0xec, 0xf9, 0xf1);
    private static final DeviceRgb BORDER = new DeviceRgb(0xb7, 0xd1, 0xc3);
    private static final DeviceRgb LABEL_BG = new DeviceRgb(0xf1, 0xf7, 0xf3);
    private static final DeviceRgb GOLD = new DeviceRgb(0xf5, 0x9e, 0x0b);
    private static final DeviceRgb NAVY = new DeviceRgb(0x0b, 0x3b, 0x68);
    private static final DeviceRgb MUTED = new DeviceRgb(0x4b, 0x55, 0x63);

    public byte[] generate(PaymentReceipt receipt, PaymentTransaction txn) {
        return generate(receipt, txn, null, null);
    }

    public byte[] generate(PaymentReceipt receipt, PaymentTransaction txn, String signatureBase64, String sealBase64) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            PdfPage page = pdf.addNewPage(PageSize.A4);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(40, 40, 34, 40);

            addPageBorder(page);
            addHeader(doc, receipt);
            addStatusBand(doc, receipt);
            addDetails(doc, receipt, txn);
            addAmountBox(doc, receipt);
            addAuthority(doc, signatureBase64, sealBase64);
            addFooter(doc, receipt);

            doc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void addPageBorder(PdfPage page) {
        float w = PageSize.A4.getWidth();
        float h = PageSize.A4.getHeight();
        PdfCanvas canvas = new PdfCanvas(page);
        canvas.setStrokeColor(GREEN_DARK)
            .setLineWidth(2f)
            .rectangle(new Rectangle(20, 20, w - 40, h - 40))
            .stroke();
        canvas.setStrokeColor(GOLD)
            .setLineWidth(0.8f)
            .rectangle(new Rectangle(26, 26, w - 52, h - 52))
            .stroke();
        canvas.release();
    }

    private void addHeader(Document doc, PaymentReceipt receipt) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBorder(Border.NO_BORDER)
            .setMarginBottom(10);

        Cell h = new Cell()
            .setBorder(Border.NO_BORDER)
            .setPaddingTop(0)
            .setPaddingBottom(4)
            .setTextAlignment(TextAlignment.CENTER);

        addClasspathImage(h, "static/logo.png", 52, 52);
        h.add(new Paragraph("Government of the People's Republic of Bangladesh")
            .setFontSize(11)
            .setBold()
            .setFontColor(GREEN_DARK)
            .setMarginBottom(2));
        h.add(new Paragraph("Municipal E-Governance Portal")
            .setFontSize(13)
            .setBold()
            .setFontColor(ColorConstants.BLACK)
            .setMarginBottom(3));
        h.add(new Paragraph(titleFor(receipt))
            .setFontSize(17)
            .setBold()
            .setFontColor(new DeviceRgb(0xb9, 0x1c, 0x1c))
            .setMarginBottom(0));

        header.addCell(h);
        doc.add(header);

        doc.add(new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setHeight(2)
            .setBackgroundColor(GREEN_DARK)
            .setBorder(Border.NO_BORDER)
            .setMarginBottom(10));
    }

    private void addStatusBand(Document doc, PaymentReceipt receipt) {
        Table band = new Table(UnitValue.createPercentArray(new float[]{1.1f, 1.5f, 1.1f}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBorder(new SolidBorder(BORDER, 1))
            .setBackgroundColor(GREEN_LIGHT)
            .setMarginBottom(12);

        band.addCell(infoCell("Receipt No.", receipt.getReceiptNo(), TextAlignment.LEFT));
        band.addCell(infoCell("Payment Status", "PAID", TextAlignment.CENTER).setFontColor(GREEN_DARK));
        band.addCell(infoCell("Issue Date", date(receipt), TextAlignment.RIGHT));
        doc.add(band);
    }

    private void addDetails(Document doc, PaymentReceipt receipt, PaymentTransaction txn) {
        Table section = sectionTitle("Citizen & Payment Details");
        doc.add(section);

        Table details = new Table(UnitValue.createPercentArray(new float[]{2.15f, 4.85f}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBorder(new SolidBorder(BORDER, 1))
            .setMarginBottom(12);

        addRow(details, "Citizen Name", value(receipt.getCitizenName()));
        addRow(details, "NID Number", value(receipt.getCitizenNid()));
        addRow(details, "Mobile Number", txn != null ? value(txn.getMobile()) : "-");
        addRow(details, "Email Address", txn != null ? value(txn.getEmail()) : "-");
        addRow(details, "Service Type", serviceLabel(receipt.getServiceType()));
        addRow(details, "Bill Description", value(receipt.getDescription()));
        addRow(details, "Payment Method", value(receipt.getMethod()));
        addRow(details, "Transaction Ref.", txn != null ? value(txn.getTxnRef()) : "-");
        addRow(details, "Payment Time", date(receipt));

        doc.add(details);
    }

    private void addAmountBox(Document doc, PaymentReceipt receipt) {
        Table amount = new Table(UnitValue.createPercentArray(new float[]{1.4f, 1f}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBackgroundColor(GREEN_LIGHT)
            .setBorder(new SolidBorder(GREEN_DARK, 1.5f))
            .setMarginBottom(14);

        amount.addCell(new Cell()
            .add(new Paragraph("Total Paid Amount").setFontSize(12).setBold().setFontColor(GREEN_DARK))
            .add(new Paragraph("This receipt confirms successful payment for the service mentioned above.")
                .setFontSize(8)
                .setFontColor(MUTED)
                .setMarginTop(2))
            .setBorder(Border.NO_BORDER)
            .setPadding(12));

        amount.addCell(new Cell()
            .add(new Paragraph(String.format("BDT %,.2f", receipt.getAmount()))
                .setFontSize(19)
                .setBold()
                .setFontColor(NAVY)
                .setTextAlignment(TextAlignment.RIGHT))
            .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
            .setBorder(Border.NO_BORDER)
            .setPadding(12));

        doc.add(amount);
    }

    private void addAuthority(Document doc, String signatureBase64, String sealBase64) {
        Table auth = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setMarginBottom(8);

        auth.addCell(authorityCell("Prepared By", "Accounts Section", signatureBase64, sealBase64, TextAlignment.LEFT));
        auth.addCell(authorityCell("Authorized Signature", "Water Supply Authority", signatureBase64, sealBase64, TextAlignment.RIGHT));

        doc.add(auth);
    }

    private Cell authorityCell(String title, String subTitle, String signatureBase64, String sealBase64, TextAlignment align) {
        Cell cell = new Cell()
            .setBorder(Border.NO_BORDER)
            .setTextAlignment(align)
            .setPaddingTop(4);

        if (signatureBase64 != null && !signatureBase64.isBlank()) {
            addImage(cell, signatureBase64, 108, 34, align);
        } else {
            cell.add(new Paragraph("____________________").setFontSize(9).setMarginBottom(5));
        }

        if (sealBase64 != null && !sealBase64.isBlank()) {
            addImage(cell, sealBase64, 82, 44, align);
        } else {
            addDefaultSeal(cell, align);
        }

        cell.add(new Paragraph(title).setFontSize(9).setBold().setFontColor(GREEN_DARK).setMarginBottom(1));
        cell.add(new Paragraph(subTitle).setFontSize(8).setFontColor(MUTED).setMarginTop(0));
        return cell;
    }

    private void addFooter(Document doc, PaymentReceipt receipt) {
        Table footer = new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBorderTop(new SolidBorder(BORDER, 1));

        footer.addCell(new Cell()
            .add(new Paragraph("Verify online: municipality.gov.bd | Receipt No: " + value(receipt.getReceiptNo()))
                .setFontSize(8)
                .setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(8)
                .setMarginBottom(2))
            .add(new Paragraph("Generated by Municipal E-Governance System | " + Year.now().getValue())
                .setFontSize(8)
                .setFontColor(MUTED)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(0))
            .setBorder(Border.NO_BORDER));

        doc.add(footer);
    }

    private Table sectionTitle(String title) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(UnitValue.createPercentValue(100))
            .setBackgroundColor(NAVY)
            .setBorder(Border.NO_BORDER);
        table.addCell(new Cell()
            .add(new Paragraph(title).setBold().setFontSize(10).setFontColor(ColorConstants.WHITE))
            .setPadding(7)
            .setBorder(Border.NO_BORDER));
        return table;
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell()
            .add(new Paragraph(label).setFontSize(10).setBold().setFontColor(GREEN_DARK))
            .setBackgroundColor(LABEL_BG)
            .setPadding(8)
            .setBorder(new SolidBorder(BORDER, 0.6f)));

        table.addCell(new Cell()
            .add(new Paragraph(value(value)).setFontSize(10).setFontColor(ColorConstants.BLACK))
            .setPadding(8)
            .setBorder(new SolidBorder(BORDER, 0.6f)));
    }

    private Cell infoCell(String label, String value, TextAlignment align) {
        return new Cell()
            .add(new Paragraph(label).setFontSize(8).setFontColor(MUTED).setMarginBottom(2).setTextAlignment(align))
            .add(new Paragraph(value(value)).setFontSize(10).setBold().setFontColor(ColorConstants.BLACK).setTextAlignment(align))
            .setBorder(Border.NO_BORDER)
            .setPadding(9);
    }

    private void addImage(Cell cell, String dataUrl, float width, float height) {
        addImage(cell, dataUrl, width, height, TextAlignment.CENTER);
    }

    private void addImage(Cell cell, String dataUrl, float width, float height, TextAlignment align) {
        try {
            Image image = new Image(ImageDataFactory.create(decodeDataUrl(dataUrl)))
                .setWidth(width)
                .setHeight(height);
            image.setHorizontalAlignment(horizontal(align));
            cell.add(image);
        } catch (Exception ex) {
            cell.add(new Paragraph("IMAGE NOT AVAILABLE").setFontSize(7).setBold().setFontColor(ColorConstants.RED));
        }
    }

    private void addDefaultSeal(Cell cell, TextAlignment align) {
        Table sealBox = new Table(UnitValue.createPercentArray(new float[]{1}))
            .setWidth(82)
            .setHeight(44)
            .setHorizontalAlignment(horizontal(align))
            .setBorder(new SolidBorder(GREEN_DARK, 0.7f));
        Cell inner = new Cell().setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER).setPadding(2);
        addClasspathImage(inner, "static/logo.png", 23, 23);
        inner.add(new Paragraph("OFFICIAL SEAL").setFontSize(5.5f).setBold().setFontColor(GREEN_DARK).setMarginTop(0));
        sealBox.addCell(inner);
        cell.add(sealBox);
    }

    private void addClasspathImage(Cell cell, String path, float width, float height) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            Image image = new Image(ImageDataFactory.create(resource.getInputStream().readAllBytes()))
                .setWidth(width)
                .setHeight(height);
            image.setHorizontalAlignment(HorizontalAlignment.CENTER);
            cell.add(image);
        } catch (Exception ex) {
            cell.add(new Paragraph("LOGO").setFontSize(7).setBold().setFontColor(GREEN_DARK));
        }
    }

    private HorizontalAlignment horizontal(TextAlignment align) {
        if (align == TextAlignment.RIGHT) return HorizontalAlignment.RIGHT;
        if (align == TextAlignment.LEFT) return HorizontalAlignment.LEFT;
        return HorizontalAlignment.CENTER;
    }

    private byte[] decodeDataUrl(String dataUrl) {
        String raw = dataUrl != null && dataUrl.contains(",") ? dataUrl.substring(dataUrl.indexOf(',') + 1) : dataUrl;
        return Base64.getDecoder().decode(raw);
    }

    private String titleFor(PaymentReceipt receipt) {
        return "WaterBill".equals(receipt.getServiceType())
            ? "Official Water Bill Payment Receipt"
            : "Official Payment Receipt";
    }

    private String date(PaymentReceipt receipt) {
        return receipt.getIssuedAt() != null
            ? receipt.getIssuedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
            : "-";
    }

    private String value(String value) {
        return value != null && !value.isBlank() ? value : "-";
    }

    private String serviceLabel(String type) {
        if (type == null || type.isBlank()) return "-";
        return switch (type) {
            case "WaterBill" -> "Water Bill";
            case "TradeLicense" -> "Trade License";
            case "HoldingTax" -> "Holding Tax";
            case "ETender" -> "E-Tender";
            case "BirthCertificate" -> "Birth Certificate";
            default -> type;
        };
    }
}
