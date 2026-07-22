package com.mgt.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import com.mgt.model.FamilyCard;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class FamilyCardPdfService {

    // ================= GOVT STYLE COLORS =================
    private static final DeviceRgb GOVT_GREEN = new DeviceRgb(0, 106, 78);
    private static final DeviceRgb GOVT_RED = new DeviceRgb(244, 42, 65);
    private static final DeviceRgb TEXT_DARK = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb TEXT_MUTED = new DeviceRgb(110, 110, 110);
    private static final DeviceRgb CARD_BG = new DeviceRgb(250, 250, 252);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(210, 215, 220);
    
    // Smart Chip Colors
    private static final DeviceRgb CHIP_GOLD = new DeviceRgb(230, 180, 60);
    private static final DeviceRgb CHIP_LINE = new DeviceRgb(180, 135, 30);

    // ================= PROFESSIONAL CR-80 CARD SIZE =================
    private static final float CARD_W = 324f; 
    private static final float CARD_H = 204f; 

    // ================= PATHS =================
    private static final String UPLOAD_BASE = "src/main/resources/uploads/";
    private static final String GOVT_LOGO = "src/main/resources/static/logo.png";
    private static final String TCB_LOGO = "src/main/resources/static/tcb.png";
    private static final String AUTH_SIGNATURE = "src/main/resources/static/signature.png";

    // ================= MAIN =================
    public byte[] generateCard(FamilyCard card) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);

            PageSize pageSize = PageSize.A4.rotate();
            pdf.addNewPage(pageSize);

            Document doc = new Document(pdf);
            doc.setMargins(0, 0, 0, 0);

            float pageW = pageSize.getWidth();
            float pageH = pageSize.getHeight();

            float x = (pageW - CARD_W) / 2f;
            float y = (pageH - CARD_H) / 2f;

            PdfCanvas canvas = new PdfCanvas(pdf.getPage(1));

            // ================= CARD BACKGROUND =================
            canvas.saveState()
                    .setFillColor(CARD_BG)
                    .roundRectangle(x, y, CARD_W, CARD_H, 8)
                    .fill()
                    .restoreState();

            // ================= BORDER =================
            canvas.saveState()
                    .setStrokeColor(BORDER_COLOR)
                    .setLineWidth(1.0f)
                    .roundRectangle(x, y, CARD_W, CARD_H, 8)
                    .stroke()
                    .restoreState();

            // ================= HEADER =================
            canvas.saveState()
                    .setFillColor(GOVT_GREEN)
                    .roundRectangle(x, y + CARD_H - 47, CARD_W, 47, 8)
                    .fill()
                    .restoreState();

            canvas.saveState()
                    .setFillColor(GOVT_GREEN)
                    .rectangle(x, y + CARD_H - 47, CARD_W, 12)
                    .fill()
                    .restoreState();

            canvas.saveState()
                    .setFillColor(GOVT_RED)
                    .rectangle(x, y + CARD_H - 49, CARD_W, 2)
                    .fill()
                    .restoreState();

            // ================= FOOTER =================
            canvas.saveState()
                    .setFillColor(GOVT_GREEN)
                    .roundRectangle(x, y, CARD_W, 18, 8)
                    .fill()
                    .restoreState();

            canvas.saveState()
                    .setFillColor(GOVT_GREEN)
                    .rectangle(x, y + 10, CARD_W, 8)
                    .fill()
                    .restoreState();

            // ================= HEADER LOGOS & TEXT =================
            float logoY = y + CARD_H - 38; 
            addImage(doc, GOVT_LOGO, x + 10, logoY, 30, 30);
            addImage(doc, TCB_LOGO, x + CARD_W - 40, logoY, 30, 30);

            addText(doc, "গণপ্রজাতন্ত্রী বাংলাদেশ সরকার", x, y + CARD_H - 12, CARD_W, 7.5f, WHITE, TextAlignment.CENTER, true);
            addText(doc, "Government of the People's Republic of Bangladesh", x, y + CARD_H - 20, CARD_W, 5.5f, WHITE, TextAlignment.CENTER, false);
            addText(doc, "Ministry of Commerce", x, y + CARD_H - 28, CARD_W, 5.5f, WHITE, TextAlignment.CENTER, false);
            addText(doc, "SMART FAMILY CARD", x, y + CARD_H - 41, CARD_W, 9.5f, WHITE, TextAlignment.CENTER, true);

            // ================= PHOTO =================
            float photoX = x + 12;
            float photoY = y + 62; 
            float photoW = 66;
            float photoH = 80;

            canvas.saveState()
                    .setFillColor(WHITE)
                    .setStrokeColor(BORDER_COLOR)
                    .setLineWidth(0.8f)
                    .roundRectangle(photoX - 2, photoY - 2, photoW + 4, photoH + 4, 3)
                    .fillStroke()
                    .restoreState();

            byte[] photoBytes = loadBytes(card.getPhotoUrl());
            if (photoBytes != null) {
                Image photo = new Image(ImageDataFactory.create(photoBytes));
                photo.setFixedPosition(photoX, photoY);
                photo.setWidth(photoW);
                photo.setHeight(photoH);
                doc.add(photo);
            }

            // ================= ADDRESS =================
            addLabel(doc, "Address:", x + 12, y + 48, 70);
            addText(doc, safe(card.getAddress()), x + 12, y + 24, 80, 5.5f, TEXT_DARK, TextAlignment.LEFT, false);

            // ================= INFO FIELDS =================
            float infoX = x + 96;
            float infoY = y + 138; 
            float rowGap = 19f; 

            // 1. Name
            addLabel(doc, "Name", infoX, infoY, 150);
            addValue(doc, safe(card.getHolderName()), infoX, infoY - 9, 150);

            // 2. Card Number
            addLabel(doc, "Card Number", infoX, infoY - rowGap, 150);
            addValue(doc, safe(card.getCardNo()), infoX, infoY - rowGap - 9, 150);

            // 3. National ID (NID)
            addLabel(doc, "National ID (NID)", infoX, infoY - (rowGap * 2), 150);
            addValue(doc, safe(card.getNid()), infoX, infoY - (rowGap * 2) - 9, 150);

            // 4. Father / Husband Name
            addLabel(doc, "Father / Husband Name", infoX, infoY - (rowGap * 3), 150);
            addValue(doc, safe(card.getHusbandOrFatherName()), infoX, infoY - (rowGap * 3) - 9, 150);

            // 5. Mobile & Occupation
            float lastRowY = infoY - (rowGap * 4);
            addLabel(doc, "Mobile", infoX, lastRowY, 80);
            addValue(doc, safe(card.getContact()), infoX, lastRowY - 9, 80);

            float occupationX = infoX + 85;
            addLabel(doc, "Occupation", occupationX, lastRowY, 70);
            addValue(doc, safe(card.getOccupation()), occupationX, lastRowY - 9, 70);

            // ================= ELECTRONIC CHIP =================
            float chipX = occupationX; 
            float chipY = lastRowY + 23; 
            float chipW = 28f;
            float chipH = 22f;

            canvas.saveState()
                    .setFillColor(CHIP_GOLD)
                    .setStrokeColor(CHIP_LINE)
                    .setLineWidth(0.6f)
                    .roundRectangle(chipX, chipY, chipW, chipH, 4)
                    .fillStroke()
                    .restoreState();

            canvas.saveState()
                    .setStrokeColor(CHIP_LINE)
                    .setLineWidth(0.5f)
                    .moveTo(chipX, chipY + (chipH / 2))
                    .lineTo(chipX + chipW, chipY + (chipH / 2))
                    .moveTo(chipX + (chipW / 3), chipY)
                    .lineTo(chipX + (chipW / 3), chipY + chipH)
                    .moveTo(chipX + (2 * chipW / 3), chipY)
                    .lineTo(chipX + (2 * chipW / 3), chipY + chipH)
                    .rectangle(chipX + (chipW / 4), chipY + (chipH / 4), chipW / 2, chipH / 2)
                    .stroke()
                    .restoreState();

            // ================= DATE OF BIRTH (TOP OF RIGHT PANEL) =================
            float qrX = x + CARD_W - 64;
            float qrSize = 52;
            
            float dobY = y + 128; 
            addText(doc, "Date of Birth", qrX - 8, dobY + 8, qrSize + 16, 6.5f, TEXT_MUTED, TextAlignment.CENTER, false);
            addText(doc, safe(card.getDateOfBirth()), qrX - 8, dobY, qrSize + 16, 7.5f, TEXT_DARK, TextAlignment.CENTER, true);

            // ================= QR CODE (CENTER OF RIGHT PANEL) =================
            float qrY = y + 72; 

            canvas.saveState()
                    .setFillColor(WHITE)
                    .setStrokeColor(BORDER_COLOR)
                    .setLineWidth(0.8f)
                    .roundRectangle(qrX - 2, qrY - 2, qrSize + 4, qrSize + 4, 4)
                    .fillStroke()
                    .restoreState();

            String qrData = "EGOV_CARD|TYPE=FAMILY|CARD_NO=" + safe(card.getCardNo());
            byte[] qrBytes = generateQR(qrData);
            if (qrBytes != null) {
                Image qr = new Image(ImageDataFactory.create(qrBytes));
                qr.setFixedPosition(qrX, qrY);
                qr.setWidth(qrSize);
                qr.setHeight(qrSize);
                doc.add(qr);
            }

            // ================= AUTHORIZED SIGNATURE (MOVED UP SAFELY) =================
            float sigW = 38f; 
            float sigH = 13f; 
            float sigX = qrX + ((qrSize - sigW) / 2f); 
            // sigY-কে বাড়িয়ে y + 43 করা হয়েছে, এবং বারকোড y + 24 এ থাকায় এখন মাঝে ১৯ পয়েন্টের বিশাল এবং নিরাপদ গ্যাপ থাকবে
            float sigY = y + 43; 

            canvas.saveState()
                    .setFillColor(WHITE)
                    .roundRectangle(sigX - 2, sigY + 8, sigW + 4, sigH + 4, 2)
                    .fill()
                    .restoreState();

            addSignatureImage(doc, card.getCertificateSignature(), AUTH_SIGNATURE, sigX, sigY + 10, sigW, sigH);
            addText(doc, "Authorized Signature", qrX - 8, sigY, qrSize + 16, 5.5f, TEXT_MUTED, TextAlignment.CENTER, false);

            // ================= BARCODE =================
            // বারকোডের পজিশন নিখুঁতভাবে y + 24 এ ফিক্সড রাখা হয়েছে
            float bcX = x + 96;
            float bcY = y + 24;
            float bcW = CARD_W - 108;

            byte[] barcode = generateBarcode(safe(card.getCardNo()));
            if (barcode != null) {
                Image bc = new Image(ImageDataFactory.create(barcode));
                bc.setFixedPosition(bcX, bcY);
                bc.setWidth(bcW);
                bc.setHeight(16);
                doc.add(bc);
            }

            // ================= FOOTER TEXT =================
            addText(
                    doc,
                    "Trading Corporation of Bangladesh (TCB)",
                    x,
                    y + 5,
                    CARD_W,
                    7.5f,
                    WHITE,
                    TextAlignment.CENTER,
                    false
            );

            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    // ================= HELPERS =================

    private void addLabel(Document doc, String text, float x, float y, float width) {
        addText(doc, text, x, y, width, 6.5f, TEXT_MUTED, TextAlignment.LEFT, false);
    }

    private void addValue(Document doc, String text, float x, float y, float width) {
        addText(doc, text, x, y, width, 8f, TEXT_DARK, TextAlignment.LEFT, true);
    }

    private void addText(Document doc, String text, float x, float y,
                         float width, float size, DeviceRgb color,
                         TextAlignment align, boolean bold) {

        Paragraph p = new Paragraph(text)
                .setFontSize(size)
                .setFontColor(color)
                .setTextAlignment(align)
                .setFixedPosition(x, y, width);

        if (bold) p.setBold();
        doc.add(p);
    }


    private void addSignatureImage(Document doc, String dataUrl, String fallbackPath, float x, float y, float w, float h) {
        try {
            Image img;
            if (dataUrl != null && !dataUrl.isBlank()) {
                String raw = dataUrl.contains(",") ? dataUrl.substring(dataUrl.indexOf(',') + 1) : dataUrl;
                img = new Image(ImageDataFactory.create(Base64.getDecoder().decode(raw)));
            } else {
                File file = new File(fallbackPath);
                if (!file.exists()) return;
                img = new Image(ImageDataFactory.create(fallbackPath));
            }
            img.setFixedPosition(x, y);
            img.setWidth(w);
            img.setHeight(h);
            doc.add(img);
        } catch (Exception ignored) {}
    }

    private void addImage(Document doc, String path, float x, float y, float w, float h) {
        try {
            File file = new File(path);
            if (!file.exists()) return;

            Image img = new Image(ImageDataFactory.create(path));
            img.setFixedPosition(x, y);
            img.setWidth(w);
            img.setHeight(h);
            doc.add(img);
        } catch (Exception ignored) {}
    }

    private byte[] generateQR(String data) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 0);

            BitMatrix matrix = new MultiFormatWriter().encode(
                    data, BarcodeFormat.QR_CODE, 150, 150, hints
            );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] generateBarcode(String data) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    data, BarcodeFormat.CODE_128, 400, 50
            );

            BufferedImage image = new BufferedImage(400, 50, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 400; x++) {
                for (int y = 0; y < 50; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] loadBytes(String photoUrl) {
        try {
            if (photoUrl == null || photoUrl.isBlank()) return null;
            String fileName = photoUrl.startsWith("uploads/") ? photoUrl.substring(8) : photoUrl;
            File file = Paths.get(UPLOAD_BASE, fileName).toFile();
            if (file.exists()) {
                return Files.readAllBytes(file.toPath());
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "N/A" : s;
    }
}