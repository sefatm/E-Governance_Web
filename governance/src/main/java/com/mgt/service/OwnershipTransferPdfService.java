package com.mgt.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.mgt.model.OwnershipTransfer;

@Service
public class OwnershipTransferPdfService {

    private static final DeviceRgb C_BROWN  = new DeviceRgb(100, 30, 0);
    private static final DeviceRgb C_GREEN  = new DeviceRgb(0, 80, 0);
    private static final DeviceRgb C_TEXT   = new DeviceRgb(40, 40, 40);
    private static final DeviceRgb C_BORDER = new DeviceRgb(200, 180, 170);

    public byte[] generateCertificate(OwnershipTransfer o) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter   writer = new PdfWriter(out);
            PdfDocument pdf    = new PdfDocument(writer);
            pdf.addNewPage();

            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(35, 40, 35, 40);

            PdfPage page = pdf.getFirstPage();
            float W = PageSize.A4.getWidth();
            float H = PageSize.A4.getHeight();

            // Border + Watermark 
            drawBorder(new PdfCanvas(page), W, H);
            drawWatermark(pdf, W, H);

            // Logo 
            ClassPathResource logoRes = new ClassPathResource("static/logo.png");
            Image logo = new Image(ImageDataFactory.create(logoRes.getInputStream().readAllBytes()));
            logo.setWidth(75);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
            logo.setMarginBottom(6);
            doc.add(logo);

            // Header 
            doc.add(new Paragraph("Government of the People's Republic of Bangladesh")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12).setBold().setFontColor(C_GREEN));

            doc.add(new Paragraph("Madaripur Municipality Office")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14).setBold());

            doc.add(new Paragraph("OWNERSHIP TRANSFER CERTIFICATE")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16).setBold()
                    .setFontColor(ColorConstants.RED)
                    .setMarginBottom(10));

            // Issue date + Serial 
            String today  = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String serial = "OWT-" + o.getId() + "-" + (System.currentTimeMillis() % 100000);

            Table head = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            head.setWidth(UnitValue.createPercentValue(100));
            head.addCell(new Cell()
                    .add(new Paragraph("Transfer ID: " + o.getId()).setBold())
                    .setBorder(null));
            head.addCell(new Cell()
                    .add(new Paragraph("Issue Date: " + today + "\nSerial No: " + serial))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(null));
            doc.add(head);

            // Data Table 
            Table table = new Table(UnitValue.createPercentArray(new float[]{38, 62}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(15);

            addRow(table, "Current Owner Name",  o.getCurrentOwner());
            addRow(table, "Current Owner NID",   orDash(o.getCurrentOwnerNid()));
            addRow(table, "New Owner Name",      o.getNewOwner());
            addRow(table, "New Owner NID",       orDash(o.getNewOwnerNid()));
            addRow(table, "Holding Number",      o.getHoldingNumber());
            addRow(table, "Ward No",             orDash(o.getWardNo()));
            addRow(table, "Property Address",    o.getAddress());
            addRow(table, "Transfer Type",       orDash(o.getRelationship()));
            addRow(table, "Reason",              o.getReason());
            addRow(table, "Contact",             o.getContact());
            addRow(table, "Status",              o.getStatus());

            doc.add(table);

            // Declaration
            doc.add(new Paragraph(
                    "This is to certify that the ownership of the above holding has been duly " +
                    "transferred and verified by the competent authority.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9).setFontColor(C_TEXT).setItalic()
                    .setMarginTop(12));

            // QR 
            String qrData = "ID:"      + o.getId()
                    + "|HOLDING:"      + o.getHoldingNumber()
                    + "|FROM:"         + o.getCurrentOwner()
                    + "|TO:"           + o.getNewOwner()
                    + "|WARD:"         + orDash(o.getWardNo())
                    + "|TYPE:"         + orDash(o.getRelationship());

            Image qr = new Image(ImageDataFactory.create(generateQr(qrData, 120)));
            qr.setWidth(90).setHeight(90)
              .setHorizontalAlignment(HorizontalAlignment.CENTER)
              .setMarginTop(12);
            doc.add(qr);

            // Signatures + Seals
            Table sign = new Table(UnitValue.createPercentArray(new float[]{50, 50}));
            sign.setWidth(UnitValue.createPercentValue(100)).setMarginTop(24);
            sign.addCell(signatureCell(o.getFirstSignature(), o.getFirstSeal(), "First Approving Officer", TextAlignment.LEFT));
            sign.addCell(signatureCell(o.getSecondSignature(), o.getSecondSeal(), "Final Approving Officer", TextAlignment.RIGHT));
            doc.add(sign);

            // Footer
            doc.add(new Paragraph("Verify certificate using QR Code | Madaripur Municipality System")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8).setFontColor(ColorConstants.GRAY).setMarginTop(20));
            doc.add(new Paragraph("© Madaripur Municipality")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(7).setFontColor(ColorConstants.GRAY));

            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    // Helpers 

    private String orDash(String val) {
        return (val == null || val.isBlank()) ? "—" : val;
    }

    private void drawBorder(PdfCanvas canvas, float W, float H) {
        canvas.setStrokeColor(C_BROWN)
                .setLineWidth(2f)
                .rectangle(new Rectangle(20, 20, W - 40, H - 40))
                .stroke();
    }

    private void drawWatermark(PdfDocument pdf, float W, float H) {
        try {
            ClassPathResource logoRes = new ClassPathResource("static/logo.png");
            byte[]    logoBytes = logoRes.getInputStream().readAllBytes();
            PdfPage   page      = pdf.getFirstPage();
            PdfCanvas canvas    = new PdfCanvas(
                    page.newContentStreamBefore(), page.getResources(), pdf);

            PdfExtGState gs = new PdfExtGState();
            gs.setFillOpacity(0.10f);

            canvas.saveState();
            canvas.setExtGState(gs);

            float size = 260f;
            canvas.addImageFittedIntoRectangle(
                    ImageDataFactory.create(logoBytes),
                    new Rectangle((W - size) / 2, (H - size) / 2, size, size),
                    false);
            canvas.restoreState();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setBold().setFontSize(9).setFontColor(C_BROWN))
                .setPadding(6)
                .setBorder(new SolidBorder(C_BORDER, 0.5f)));
        table.addCell(new Cell()
                .add(new Paragraph(value == null || value.isBlank() ? "—" : value).setFontSize(9))
                .setPadding(6)
                .setBorder(new SolidBorder(C_BORDER, 0.5f)));
    }


    private Cell signatureCell(String signatureBase64, String sealBase64, String title, TextAlignment align) {
        Cell cell = new Cell().setBorder(null).setPadding(4).setTextAlignment(align);
        try {
            if (signatureBase64 != null && !signatureBase64.isBlank()) {
                Image sig = new Image(ImageDataFactory.create(decodeDataUrl(signatureBase64)))
                        .setWidth(100).setHeight(38);
                sig.setHorizontalAlignment(align == TextAlignment.RIGHT ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
                cell.add(sig);
            } else {
                cell.add(new Paragraph("SIGNATURE MISSING").setFontSize(8).setBold().setFontColor(ColorConstants.RED).setTextAlignment(align));
            }
        } catch (Exception ex) {
            cell.add(new Paragraph("INVALID SIGNATURE").setFontSize(8).setBold().setFontColor(ColorConstants.RED).setTextAlignment(align));
        }
        cell.add(new Paragraph(title).setFontSize(8.5f).setBold().setTextAlignment(align));
        try {
            if (sealBase64 != null && !sealBase64.isBlank()) {
                Image seal = new Image(ImageDataFactory.create(decodeDataUrl(sealBase64)))
                        .setWidth(78).setHeight(45);
                seal.setHorizontalAlignment(align == TextAlignment.RIGHT ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
                cell.add(seal);
            } else {
                cell.add(new Paragraph("SEAL MISSING").setFontSize(8).setBold().setFontColor(ColorConstants.RED).setTextAlignment(align));
            }
        } catch (Exception ex) {
            cell.add(new Paragraph("INVALID SEAL").setFontSize(8).setBold().setFontColor(ColorConstants.RED).setTextAlignment(align));
        }
        return cell;
    }

    private byte[] decodeDataUrl(String dataUrl) {
        String data = dataUrl == null ? "" : dataUrl.trim();
        int comma = data.indexOf(',');
        if (comma >= 0) data = data.substring(comma + 1);
        return java.util.Base64.getDecoder().decode(data);
    }

    private byte[] generateQr(String data, int size) throws Exception {
        QRCodeWriter writer = new QRCodeWriter();
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);

        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }
}
