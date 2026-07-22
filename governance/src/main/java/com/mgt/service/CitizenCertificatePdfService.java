package com.mgt.service;

import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.*;
import com.itextpdf.kernel.geom.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import com.itextpdf.layout.*;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;

import com.mgt.model.CitizenCertificate;

import java.time.LocalDate;
import java.util.Base64;
import java.time.format.DateTimeFormatter;

@Service
public class CitizenCertificatePdfService {

    public byte[] generateCitizenCertificate(CitizenCertificate c) {
    	
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(40, 40, 40, 40);

            float W = PageSize.A4.getWidth();
            float H = PageSize.A4.getHeight();

            PdfPage page = pdf.addNewPage();
            PdfCanvas canvas = new PdfCanvas(page);

            DeviceRgb green = new DeviceRgb(0, 100, 0);

            canvas.setStrokeColor(green)
                  .setLineWidth(2)
                  .rectangle(new Rectangle(20, 20, W - 40, H - 40))
                  .stroke();

            canvas.setLineWidth(0.7f)
                  .rectangle(new Rectangle(28, 28, W - 56, H - 56))
                  .stroke();

            canvas.release();

            // Logo
            ClassPathResource logoRes = new ClassPathResource("static/logo.png");
            byte[] logoBytes = logoRes.getInputStream().readAllBytes();

            Image logo = new Image(ImageDataFactory.create(logoBytes));
            logo.setWidth(70);
            logo.setHorizontalAlignment(HorizontalAlignment.CENTER);
            doc.add(logo);

            // Watermark
            Image watermark = new Image(ImageDataFactory.create(logoBytes));
            watermark.setFixedPosition((W / 2) - 120, (H / 2) - 120);
            watermark.setWidth(240);
            watermark.setOpacity(0.08f);
            doc.add(watermark);

            // Header
            doc.add(new Paragraph("Government of the People's Republic of Bangladesh")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12)
                    .setBold()
                    .setFontColor(green));

            doc.add(new Paragraph("Union Parishad Office")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(14)
                    .setBold());

            doc.add(new Paragraph("CITIZEN CERTIFICATE")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold()
                    .setFontColor(ColorConstants.RED)
                    .setMarginBottom(10));

            // Serial + Date
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String serial = "CC-" + System.currentTimeMillis();

            Table head = new Table(new float[]{50, 50})
                    .setWidth(UnitValue.createPercentValue(100));

            head.addCell(new Cell()
                    .add(new Paragraph("Certificate No: " + safe(c.getCertificateNo())).setBold())
                    .setBorder(null));

            head.addCell(new Cell()
                    .add(new Paragraph("Issue Date: " + today + "\nSerial: " + serial))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setBorder(null));

            doc.add(head);

            // Info Table
            Table table = new Table(new float[]{35, 65})
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(15);

            addRow(table, "Name", c.getName(), green);
            addRow(table, "Father's Name", c.getFatherName(), green);
            addRow(table, "Mother's Name", c.getMotherName(), green);
            addRow(table, "Date of Birth", c.getDateOfBirth(), green);
            addRow(table, "Gender", c.getGender(), green);
            addRow(table, "NID", c.getNid(), green);
            addRow(table, "Address", c.getAddress(), green);
            addRow(table, "Permanent Address", c.getPermanentAddress(), green);
            addRow(table, "Contact", c.getContact(), green);
            addRow(table, "Purpose", c.getPurpose(), green);

            doc.add(table);

            // Declaration
            doc.add(new Paragraph(
                    "This is to certify that the above-mentioned person is a permanent resident " +
                    "of this area and the information provided is correct as per local government records.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(9)
                    .setFontColor(new DeviceRgb(60, 60, 60))
                    .setMarginTop(10));

            // QR
            doc.add(makeQrImage("ID:" + c.getId() + "|CERT:" + serial + "|NAME:" + c.getName()));

            // Signature
            doc.add(makeSignatureTable(c));

            // Footer
            doc.add(new Paragraph("Verify certificate using QR Code | Union Parishad System")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(8)
                    .setMarginTop(15));

            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    // HELPER METHODS

    private void addRow(Table table, String label, String value, DeviceRgb color) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setBold().setFontSize(9).setFontColor(color))
                .setBackgroundColor(new DeviceRgb(235, 240, 250))
                .setPadding(6));

        table.addCell(new Cell()
                .add(new Paragraph(value == null || value.isBlank() ? "—" : value).setFontSize(9))
                .setPadding(6));
    }

    private Image makeQrImage(String data) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 100, 100);
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < 100; x++)
            for (int y = 0; y < 100; y++)
                img.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);

        ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
        ImageIO.write(img, "png", qrOut);

        Image qr = new Image(ImageDataFactory.create(qrOut.toByteArray()));
        qr.setHorizontalAlignment(HorizontalAlignment.CENTER);
        qr.setMarginTop(10);
        return qr;
    }

    private Table makeSignatureTable(CitizenCertificate c) {
        Table sign = new Table(new float[]{50, 50})
                .setWidth(UnitValue.createPercentValue(100)).setMarginTop(18);
        sign.addCell(signatureCell(c.getFirstSignature(), c.getFirstSeal(), c.getFirstApprovedBy(), "First Approving Officer", false));
        sign.addCell(signatureCell(c.getSecondSignature(), c.getSecondSeal(), c.getSecondApprovedBy(), "Final Approving Officer", true));
        return sign;
    }


    private Cell signatureCell(String dataUrl, String sealUrl, String officer, String role, boolean right) {
        Cell cell = new Cell()
                .setBorder(null)
                .setMinHeight(128)
                .setPaddingTop(2)
                .setPaddingBottom(2);

        try {
            if (dataUrl != null && !dataUrl.isBlank()) {
                byte[] bytes = decodeDataUrl(dataUrl);
                Image sig = new Image(ImageDataFactory.create(bytes))
                        .setWidth(112)
                        .setHeight(34)
                        .setMarginTop(0);
                sig.setHorizontalAlignment(right ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
                cell.add(sig);
            } else {
                cell.add(new Paragraph("____________________")
                        .setMarginTop(4)
                        .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
            }
        } catch (Exception ex) {
            cell.add(new Paragraph("____________________")
                    .setMarginTop(4)
                    .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
        }

        // Do not print officer id/username. Only show officer role label.
        cell.add(new Paragraph(role)
                .setFontSize(8)
                .setMarginTop(0)
                .setMarginBottom(2)
                .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));

        cell.add(makeVisibleSealBox(sealUrl, right));
        return cell;
    }

    private Div makeVisibleSealBox(String sealUrl, boolean right) {
        Div box = new Div()
                .setWidth(104)
                .setHeight(58)
                .setPadding(1)
                .setMarginTop(1)
                .setBorder(null);
        box.setHorizontalAlignment(right ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
        try {
            if (sealUrl == null || sealUrl.isBlank()) {
                box.add(new Paragraph("SEAL MISSING")
                        .setFontSize(7).setBold().setFontColor(ColorConstants.RED)
                        .setTextAlignment(TextAlignment.CENTER));
                return box;
            }
            Image seal = sealImage(sealUrl);
            seal.setWidth(98).setHeight(52).setOpacity(1.0f)
                    .setHorizontalAlignment(HorizontalAlignment.CENTER);
            box.add(seal);
            return box;
        } catch (Exception ex) {
            box.add(new Paragraph("INVALID SEAL")
                    .setFontSize(7).setBold().setFontColor(ColorConstants.RED)
                    .setTextAlignment(TextAlignment.CENTER));
            return box;
        }
    }

    private byte[] decodeDataUrl(String dataUrl) {
        String raw = dataUrl.contains(",") ? dataUrl.substring(dataUrl.indexOf(',') + 1) : dataUrl;
        return Base64.getDecoder().decode(raw);
    }

    private Image sealImage(String sealUrl) throws Exception {
        if (sealUrl == null || sealUrl.isBlank()) {
            throw new IllegalArgumentException("Seal image is missing");
        }
        byte[] bytes = decodeDataUrl(sealUrl);
        return new Image(ImageDataFactory.create(bytes));
    }

    private String safe(Object v) {
        return v == null ? "" : v.toString();
    }
}
