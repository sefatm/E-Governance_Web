package com.mgt.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
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
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import com.mgt.model.HoldingNewRegistration;

@Service
public class HoldingNewRegistrationPdfService {

    // ═════════════════════════════════════════════
    // COLORS
    // ═════════════════════════════════════════════

    private static final DeviceRgb C_GREEN =
            new DeviceRgb(0, 80, 0);

    private static final DeviceRgb C_BLUE =
            new DeviceRgb(0, 60, 120);

    private static final DeviceRgb C_RED =
            new DeviceRgb(185, 0, 0);

    private static final DeviceRgb C_TEXT =
            new DeviceRgb(40, 40, 40);

    private static final DeviceRgb C_LABEL_BG =
            new DeviceRgb(230, 240, 255);

    private static final DeviceRgb C_VALUE_BG =
            new DeviceRgb(248, 250, 252);

    private static final DeviceRgb C_BORDER =
            new DeviceRgb(180, 190, 210);

    // ═════════════════════════════════════════════
    // MAIN PDF METHOD
    // ═════════════════════════════════════════════

    public byte[] generateCertificate(HoldingNewRegistration h) {

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        try {

            PdfWriter writer = new PdfWriter(out);

            PdfDocument pdf =
                    new PdfDocument(writer);

            // FIRST PAGE
            pdf.addNewPage();

            Document doc =
                    new Document(pdf, PageSize.A4);

            doc.setMargins(35, 40, 35, 40);

            PdfPage page = pdf.getFirstPage();

            float W = PageSize.A4.getWidth();
            float H = PageSize.A4.getHeight();

            // ═════════════════════════════════════
            // BORDER
            // ═════════════════════════════════════

            PdfCanvas canvas =
                    new PdfCanvas(page);

            drawBorder(canvas, W, H);

            // ═════════════════════════════════════
            // WATERMARK
            // ═════════════════════════════════════

            drawWatermark(pdf, W, H);

            // ═════════════════════════════════════
            // LOGO
            // ═════════════════════════════════════

            ClassPathResource logoRes =
                    new ClassPathResource(
                            "static/logo.png");

            Image logo = new Image(
                    ImageDataFactory.create(
                            logoRes.getInputStream()
                                    .readAllBytes()
                    )
            );

            logo.setWidth(65);
            logo.setHorizontalAlignment(
                    HorizontalAlignment.CENTER);

            logo.setMarginBottom(3);

            doc.add(logo);

            // ═════════════════════════════════════
            // HEADER
            // ═════════════════════════════════════

            doc.add(
                    new Paragraph(
                            "Government of the People's Republic of Bangladesh")
                            .setTextAlignment(
                                    TextAlignment.CENTER)
                            .setFontSize(10)
                            .setBold()
                            .setFontColor(C_GREEN)
                            .setMarginBottom(0)
            );

            doc.add(
                    new Paragraph(
                            "Madaripur Municipality Office")
                            .setTextAlignment(
                                    TextAlignment.CENTER)
                            .setFontSize(12)
                            .setBold()
                            .setMarginBottom(0)
            );

            doc.add(
                    new Paragraph(
                            "Holding Registration Branch")
                            .setTextAlignment(
                                    TextAlignment.CENTER)
                            .setFontSize(8)
                            .setFontColor(
                                    ColorConstants.DARK_GRAY)
                            .setMarginBottom(3)
            );

            doc.add(
                    new Paragraph(
                            "HOLDING REGISTRATION CERTIFICATE")
                            .setTextAlignment(
                                    TextAlignment.CENTER)
                            .setFontSize(15)
                            .setBold()
                            .setFontColor(C_RED)
                            .setMarginBottom(7)
            );

            // ═════════════════════════════════════
            // ISSUE INFO
            // ═════════════════════════════════════

            String today =
                    LocalDate.now()
                            .format(
                                    DateTimeFormatter
                                            .ofPattern(
                                                    "dd-MM-yyyy"));

            String serial =
                    "HLD-"
                            + safe(h.getId())
                            + "-"
                            + (System.currentTimeMillis()
                            % 100000);

            Table head =
                    new Table(
                            UnitValue.createPercentArray(
                                    new float[]{50, 50}));

            head.setWidth(
                    UnitValue.createPercentValue(100));

            head.addCell(
                    new Cell()
                            .add(
                                    new Paragraph(
                                            "Holding ID : "
                                                    + safe(h.getId()))
                                            .setBold()
                                            .setFontSize(10)
                            )
                            .setBorder(null)
            );

            head.addCell(
                    new Cell()
                            .add(
                                    new Paragraph(
                                            "Issue Date : "
                                                    + today
                                                    + "\nSerial No : "
                                                    + serial)
                                            .setFontSize(9)
                            )
                            .setTextAlignment(
                                    TextAlignment.RIGHT)
                            .setBorder(null)
            );

            doc.add(head);

            // ═════════════════════════════════════
            // DATA TABLE
            // ═════════════════════════════════════

            Table table =
                    new Table(
                            UnitValue.createPercentArray(
                                    new float[]{35, 65}));

            table.setWidth(
                    UnitValue.createPercentValue(100));

            table.setMarginTop(10);

            addRow(table,
                    "Applicant Name",
                    h.getApplicantName());

            addRow(table,
                    "Father's Name",
                    h.getFather());

            addRow(table,
                    "Mother's Name",
                    h.getMother());

            addRow(table,
                    "NID Number",
                    h.getNid());

            addRow(table, "Holding No", h.getHoldingNo());
            addRow(table, "Ward", safe(h.getWard()));
            addRow(table, "Road", h.getRoad());
            addRow(table, "Area / Mohalla", h.getArea());
            addRow(table, "Mouza", h.getMouza());
            addRow(table, "Structure Type", h.getStructureType());
            addRow(table, "Usage Type", h.getUsageType());
            addRow(table, "Ownership", h.getOwnership());
            //addRow(table, "Mobile", h.getMobile());
            addRow(table, "Address", h.getAddress());
            //addRow(table, "Status", h.getStatus());
            doc.add(table);

            // ═════════════════════════════════════
            // DECLARATION
            // ═════════════════════════════════════

            doc.add(
                    new Paragraph(
                            "This is to certify that the above holding has been duly "
                                    + "registered in the official records of Madaripur "
                                    + "Municipality and verified by the authorized authority.")
                            .setTextAlignment(
                                    TextAlignment.CENTER)
                            .setFontSize(9)
                            .setItalic()
                            .setFontColor(C_TEXT)
                            .setMarginTop(12)
            );

            // ═════════════════════════════════════
            // QR CODE
            // ═════════════════════════════════════

            String qrData =
                    "ID:" + safe(h.getId())
                            + "|HOLDING:"
                            + safe(h.getHoldingNo())
                            + "|OWNER:"
                            + safe(h.getApplicantName())
                            + "|NID:"
                            + safe(h.getNid())
                            + "|WARD:"
                            + safe(h.getWard());

            byte[] qrBytes =
                    generateQr(qrData, 100);

            Image qr = new Image(
                    ImageDataFactory.create(qrBytes));

            qr.setWidth(80);
            qr.setHeight(80);

            qr.setHorizontalAlignment(
                    HorizontalAlignment.CENTER);

            qr.setMarginTop(10);

            doc.add(qr);

            // ═════════════════════════════════════
            // 2-STEP APPROVAL SIGNATURE + SEAL
            // ═════════════════════════════════════

            doc.add(makeSignatureTable(h));

            // ═════════════════════════════════════
            // FOOTER
            // ═════════════════════════════════════

            doc.add(
                    new Paragraph(
                            "Verify certificate using QR Code | Madaripur Municipality Digital System")
                            .setTextAlignment(
                                    TextAlignment.CENTER)
                            .setFontSize(8)
                            .setFontColor(
                                    ColorConstants.GRAY)
                            .setMarginTop(18)
            );

            doc.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return out.toByteArray();
    }

    // ═════════════════════════════════════════════
    // BORDER
    // ═════════════════════════════════════════════

    private void drawBorder(PdfCanvas canvas,
                            float W,
                            float H) {

        canvas.setStrokeColor(
                        new DeviceRgb(0, 80, 150))
                .setLineWidth(2.5f)
                .rectangle(
                        new Rectangle(
                                20,
                                20,
                                W - 40,
                                H - 40))
                .stroke();

        canvas.setLineWidth(0.8f)
                .rectangle(
                        new Rectangle(
                                28,
                                28,
                                W - 56,
                                H - 56))
                .stroke();
    }

    // ═════════════════════════════════════════════
    // WATERMARK
    // ═════════════════════════════════════════════

    private void drawWatermark(PdfDocument pdf,
                               float W,
                               float H) {

        try {

            ClassPathResource logoRes =
                    new ClassPathResource(
                            "static/logo.png");

            byte[] logoBytes =
                    logoRes.getInputStream()
                            .readAllBytes();

            PdfPage page =
                    pdf.getFirstPage();

            PdfCanvas canvas =
                    new PdfCanvas(
                            page.newContentStreamBefore(),
                            page.getResources(),
                            pdf
                    );

            PdfExtGState gs =
                    new PdfExtGState();

            // TEST VALUE
            gs.setFillOpacity(0.25f);

            canvas.saveState();

            canvas.setExtGState(gs);

            float size = 250f;

            float x = (W - size) / 2;
            float y = (H - size) / 2;

            canvas.addImageFittedIntoRectangle(
                    ImageDataFactory.create(logoBytes),
                    new Rectangle(
                            x,
                            y,
                            size,
                            size),
                    false
            );

            canvas.restoreState();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════
    // TABLE ROW
    // ═════════════════════════════════════════════


    private Table makeSignatureTable(HoldingNewRegistration h) {
        Table sign = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(8)
                .setKeepTogether(true);
        sign.addCell(signatureCell(h.getFirstSignature(), h.getFirstSeal(), "First Approving Officer", false));
        sign.addCell(signatureCell(h.getSecondSignature(), h.getSecondSeal(), "Final Approving Officer", true));
        return sign;
    }

    private Cell signatureCell(String signatureUrl, String sealUrl, String role, boolean right) {
        Cell cell = new Cell().setBorder(null).setMinHeight(128).setPaddingTop(2).setPaddingBottom(2);
        try {
            if (signatureUrl != null && !signatureUrl.isBlank()) {
                Image sig = new Image(ImageDataFactory.create(decodeDataUrl(signatureUrl)))
                        .setWidth(112).setHeight(34).setMarginTop(0);
                sig.setHorizontalAlignment(right ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
                cell.add(sig);
            } else {
                cell.add(new Paragraph("____________________")
                        .setMarginTop(4)
                        .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
            }
        } catch (Exception e) {
            cell.add(new Paragraph("____________________")
                    .setMarginTop(4)
                    .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
        }
        cell.add(new Paragraph(role)
                .setFontSize(8)
                .setBold()
                .setMarginTop(0)
                .setMarginBottom(2)
                .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
        cell.add(makeVisibleSealBox(sealUrl, right));
        return cell;
    }

    private Div makeVisibleSealBox(String sealUrl, boolean right) {
        Div box = new Div().setWidth(104).setHeight(58).setPadding(1).setMarginTop(1).setBorder(null);
        box.setHorizontalAlignment(right ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
        try {
            if (sealUrl == null || sealUrl.isBlank()) {
                box.add(new Paragraph("SEAL MISSING").setFontSize(7).setBold()
                        .setFontColor(ColorConstants.RED).setTextAlignment(TextAlignment.CENTER));
                return box;
            }
            Image seal = new Image(ImageDataFactory.create(decodeDataUrl(sealUrl)))
                    .setWidth(98).setHeight(52).setOpacity(1.0f)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            box.add(seal);
        } catch (Exception ex) {
            box.add(new Paragraph("INVALID SEAL").setFontSize(7).setBold()
                    .setFontColor(ColorConstants.RED).setTextAlignment(TextAlignment.CENTER));
        }
        return box;
    }

    private byte[] decodeDataUrl(String dataUrl) {
        String raw = dataUrl != null && dataUrl.contains(",") ? dataUrl.substring(dataUrl.indexOf(',') + 1) : dataUrl;
        return Base64.getDecoder().decode(raw);
    }

    private void addRow(Table table,
                        String label,
                        String value) {

    	Cell labelCell =
    	        new Cell()
    	                .add(new Paragraph(label)
    	                        .setBold()
    	                        .setFontSize(9)
    	                        .setFontColor(C_BLUE))
    	                .setPaddingTop(5)
    	                .setPaddingBottom(5)
    	                .setPaddingLeft(10)
    	                .setBorder(new SolidBorder(C_BORDER, 0.5f));

    	Cell valueCell =
    	        new Cell()
    	                .add(new Paragraph(value == null || value.isBlank() ? "—" : value)
    	                        .setFontSize(9)
    	                        .setFontColor(C_TEXT))
    	                .setPaddingTop(5)
    	                .setPaddingBottom(5)
    	                .setPaddingLeft(10)
    	                .setBorder(new SolidBorder(C_BORDER, 0.5f));

        table.addCell(labelCell);

        table.addCell(valueCell);
    }

    // ═════════════════════════════════════════════
    // QR GENERATOR
    // ═════════════════════════════════════════════

    private byte[] generateQr(String data,
                              int size)
            throws Exception {

        QRCodeWriter qrWriter =
                new QRCodeWriter();

        BitMatrix bitMatrix =
                qrWriter.encode(
                        data,
                        BarcodeFormat.QR_CODE,
                        size,
                        size
                );

        BufferedImage qrImg =
                new BufferedImage(
                        size,
                        size,
                        BufferedImage.TYPE_INT_RGB
                );

        for (int x = 0; x < size; x++) {

            for (int y = 0; y < size; y++) {

                qrImg.setRGB(
                        x,
                        y,
                        bitMatrix.get(x, y)
                                ? 0x000000
                                : 0xFFFFFF
                );
            }
        }

        ByteArrayOutputStream qrOut =
                new ByteArrayOutputStream();

        ImageIO.write(qrImg, "png", qrOut);

        return qrOut.toByteArray();
    }

    // ═════════════════════════════════════════════
    // NULL SAFE
    // ═════════════════════════════════════════════

    private String safe(Object value) {

        return value == null
                ? ""
                : value.toString().trim();
    }
}