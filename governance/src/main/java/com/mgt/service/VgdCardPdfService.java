package com.mgt.service;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import com.mgt.model.VgdCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class VgdCardPdfService {

    @Autowired
    private TcbQrService qrService;

    // ================= VGD SMART THEME COLORS =================
    private static final DeviceRgb GOVT_MAROON = new DeviceRgb(128, 0, 32);
    private static final DeviceRgb ACCENT_GOLD  = new DeviceRgb(212, 175, 55);
    private static final DeviceRgb TEXT_DARK    = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb TEXT_MUTED   = new DeviceRgb(100, 100, 100);
    private static final DeviceRgb CARD_BG      = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb WHITE        = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(225, 215, 215);
    
    // Smart Card Chip Colors
    private static final DeviceRgb CHIP_GOLD = new DeviceRgb(239, 189, 73);
    private static final DeviceRgb CHIP_LINE = new DeviceRgb(170, 120, 20);

    // ================= PROFESSIONAL CR-80 CARD SIZE =================
    private static final float CARD_W = 324f; 
    private static final float CARD_H = 204f; 

    // ================= PATHS =================
    private static final String UPLOAD_BASE = "src/main/resources/uploads/";
    private static final String GOVT_LOGO = "src/main/resources/static/logo.png";
    private static final String MINISTRY_LOGO = "src/main/resources/static/ministry_logo.png"; 
    private static final String AUTH_SIGNATURE = "src/main/resources/static/signature.png";
    private static final String BANGLA_FONT = "src/main/resources/static/SolaimanLipi.ttf";

    public byte[] generateCard(VgdCard card) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);

            PageSize pageSize = PageSize.A4.rotate();
            pdf.addNewPage(pageSize);

            Document doc = new Document(pdf);
            doc.setMargins(0, 0, 0, 0);

            // ================= BANGLA FONT SETUP =================
            PdfFont banglaFont = null;
            File fontFile = new File(BANGLA_FONT);
            if (fontFile.exists()) {
                banglaFont = PdfFontFactory.createFont(BANGLA_FONT, com.itextpdf.kernel.pdf.PdfName.IdentityH.getValue(), PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            }

            float pageW = pageSize.getWidth();
            float pageH = pageSize.getHeight();

            float x = (pageW - CARD_W) / 2f;
            float y = (pageH - CARD_H) / 2f;

            PdfCanvas canvas = new PdfCanvas(pdf.getPage(1));

            boolean isVgf = "VGF".equalsIgnoreCase(card.getCardType());

            // ================= CARD BACKGROUND & BORDER =================
            canvas.saveState()
                    .setFillColor(CARD_BG)
                    .setStrokeColor(BORDER_COLOR)
                    .setLineWidth(1.0f)
                    .roundRectangle(x, y, CARD_W, CARD_H, 8)
                    .fillStroke()
                    .restoreState();

            // ================= HEADER BANNER =================
            canvas.saveState()
                    .setFillColor(GOVT_MAROON)
                    .roundRectangle(x, y + CARD_H - 45, CARD_W, 45, 8)
                    .fill()
                    .restoreState();

            canvas.saveState()
                    .setFillColor(GOVT_MAROON)
                    .rectangle(x, y + CARD_H - 45, CARD_W, 10)
                    .fill()
                    .restoreState();

            canvas.saveState()
                    .setFillColor(ACCENT_GOLD)
                    .rectangle(x, y + CARD_H - 47, CARD_W, 2)
                    .fill()
                    .restoreState();

            // ================= FOOTER BANNER =================
            canvas.saveState()
                    .setFillColor(GOVT_MAROON)
                    .roundRectangle(x, y, CARD_W, 16, 8)
                    .fill()
                    .restoreState();

            canvas.saveState()
                    .setFillColor(GOVT_MAROON)
                    .rectangle(x, y + 8, CARD_W, 8)
                    .fill()
                    .restoreState();

            // ================= HEADER LOGOS & TEXT (Fixed Unicode Characters) =================
            float logoY = y + CARD_H - 36; 
            addImage(doc, GOVT_LOGO, x + 12, logoY, 26, 26);
            addImage(doc, MINISTRY_LOGO, x + CARD_W - 38, logoY, 26, 26);

            addText(doc, "গণপ্রজাতন্ত্রী বাংলাদেশ সরকার", x, y + CARD_H - 12, CARD_W, 7f, WHITE, TextAlignment.CENTER, false, banglaFont);
            addText(doc, "মহিলা ও শিশু বিষয়ক মন্ত্রণালয়", x, y + CARD_H - 20, CARD_W, 6f, WHITE, TextAlignment.CENTER, false, banglaFont);
            
            String title = isVgf ? "SMART VGF CARD" : "SMART VGD CARD";
            addText(doc, title, x, y + CARD_H - 35, CARD_W, 11f, WHITE, TextAlignment.CENTER, true, null);

            // ================= PHOTO (Left Panel) =================
            float photoX = x + 12;
            float photoY = y + 68; 
            float photoW = 64;
            float photoH = 74;

            canvas.saveState()
                    .setFillColor(WHITE)
                    .setStrokeColor(BORDER_COLOR)
                    .setLineWidth(0.5f)
                    .roundRectangle(photoX - 1, photoY - 1, photoW + 2, photoH + 2, 2)
                    .fillStroke()
                    .restoreState();

            byte[] photoBytes = loadBytes(card.getPhotoUrl()); 
            if (photoBytes != null) {
                try {
                    Image photo = new Image(ImageDataFactory.create(photoBytes));
                    photo.setFixedPosition(photoX, photoY);
                    photo.setWidth(photoW);
                    photo.setHeight(photoH);
                    doc.add(photo);
                } catch (Exception e) {
                    addNoPhotoPlaceholder(doc, photoX, photoY, photoW, photoH);
                }
            } else {
                addNoPhotoPlaceholder(doc, photoX, photoY, photoW, photoH);
            }

            // ================= ADDRESS =================
            float addrY = photoY - 11;
            addText(doc, "Address / ঠিকানা:", x + 12, addrY, 75, 5.0f, TEXT_MUTED, TextAlignment.LEFT, false, banglaFont);
            String fullAddress = safe(card.getAddress()) + ", " + safe(card.getWard()) + " / " + safe(card.getUnionName());
            addText(doc, fullAddress, x + 12, addrY - 22, 75, 5.5f, TEXT_DARK, TextAlignment.LEFT, false, banglaFont);

            // ================= INFO FIELDS (Middle Panel) =================
            float infoX = x + 92;
            float infoY = y + 138; 
            float rowGap = 21f; 
            float midWidth = 110;

            // 1. Name
            addLabel(doc, "Beneficiary Name / নাম", infoX, infoY, midWidth, banglaFont);
            addValue(doc, safe(card.getHolderName()), infoX, infoY - 10, midWidth, banglaFont);

            // 2. Card Number
            addLabel(doc, "Card Number / কার্ড নং", infoX, infoY - rowGap, midWidth, banglaFont);
            addText(doc, safe(card.getCardNo()), infoX, infoY - rowGap - 10, midWidth, 8.5f, TEXT_DARK, TextAlignment.LEFT, false, null);

            // 3. National ID (NID)
            addLabel(doc, "National ID (NID) / এনআইডি", infoX, infoY - (rowGap * 2), midWidth, banglaFont);
            addText(doc, safe(card.getNid()), infoX, infoY - (rowGap * 2) - 10, midWidth, 8.5f, TEXT_DARK, TextAlignment.LEFT, false, null);

            // 4. Spouse / Father Name
            String relativeLabel = card.getHusbandName() != null && !card.getHusbandName().isBlank() ? "Husband Name / স্বামীর নাম" : "Father Name / পিতার নাম";
            String relativeValue = card.getHusbandName() != null && !card.getHusbandName().isBlank() ? card.getHusbandName() : card.getFatherName();
            addLabel(doc, relativeLabel, infoX, infoY - (rowGap * 3), midWidth, banglaFont);
            addValue(doc, safe(relativeValue), infoX, infoY - (rowGap * 3) - 10, midWidth, banglaFont);

            // 5. Mobile & Allocation Combined (Increased width for Mobile Label)
            float lastRowY = infoY - (rowGap * 4);
            addLabel(doc, "Mobile / মোবাইল", infoX, lastRowY, 68, banglaFont);
            addText(doc, safe(card.getContact()), infoX, lastRowY - 10, 60, 8.0f, TEXT_DARK, TextAlignment.LEFT, false, null);

            float allocX = infoX + 70; // Adjusted X to prevent collision
            addLabel(doc, "Allocation / বরাদ্দ", allocX, lastRowY, 65, banglaFont);
            String allocation = isVgf ? "৳ " + safe(card.getCashAmount()) : safe(card.getMonthlyRiceKg()) + " Kg Rice";
            addText(doc, allocation, allocX, lastRowY - 10, 65, 8.0f, TEXT_DARK, TextAlignment.LEFT, false, banglaFont);


            // ================= SMART CARD CHIP =================
            float chipX = x + 206;
            float chipY = y + 84;
            float chipW = 24;
            float chipH = 19;

            canvas.saveState()
                    .setFillColor(CHIP_GOLD)
                    .setStrokeColor(CHIP_LINE)
                    .setLineWidth(0.6f)
                    .roundRectangle(chipX, chipY, chipW, chipH, 4)
                    .fillStroke()
                    .moveTo(chipX + 8, chipY).lineTo(chipX + 8, chipY + chipH)
                    .moveTo(chipX + 16, chipY).lineTo(chipX + 16, chipY + chipH)
                    .moveTo(chipX, chipY + 9.5f).lineTo(chipX + chipW, chipY + 9.5f)
                    .stroke()
                    .restoreState();


            // ================= RIGHT PANEL (DOB, QR & SIGNATURE) =================
            float qrX = x + CARD_W - 64;
            float qrSize = 52;
            
            // 1. Date of Birth
            float dobY = y + 138; 
            addText(doc, "Date of Birth / জন্ম তারিখ", qrX - 15, dobY, qrSize + 25, 5.5f, TEXT_MUTED, TextAlignment.CENTER, false, banglaFont);
            addText(doc, safe(card.getDateOfBirth()), qrX - 15, dobY - 9, qrSize + 25, 8.0f, TEXT_DARK, TextAlignment.CENTER, true, null);

            // 2. QR Code
            float qrY = y + 68; 
            canvas.saveState()
                    .setFillColor(WHITE)
                    .setStrokeColor(BORDER_COLOR)
                    .setLineWidth(0.5f)
                    .roundRectangle(qrX - 1, qrY - 1, qrSize + 2, qrSize + 2, 3)
                    .fillStroke()
                    .restoreState();

            if (card.getCardNo() != null) {
                try {
                    byte[] qrBytes = qrService.generateQr("EGOV_CARD|TYPE=" + safe(card.getCardType()) + "|CARD_NO=" + safe(card.getCardNo()), 120);
                    Image qr = new Image(ImageDataFactory.create(qrBytes));
                    qr.setFixedPosition(qrX, qrY);
                    qr.setWidth(qrSize);
                    qr.setHeight(qrSize);
                    doc.add(qr);
                } catch (Exception ignored) {}
            }

            // 3. Authorized Signature
            float sigW = 34f; 
            float sigH = 11f; 
            float sigX = qrX + ((qrSize - sigW) / 2f); 
            float sigY = y + 42; 

            addSignatureImage(doc, card.getCertificateSignature(), AUTH_SIGNATURE, sigX, sigY + 8, sigW, sigH); 
            addText(doc, "Authorized Signature", qrX - 10, sigY, qrSize + 20, 5f, TEXT_MUTED, TextAlignment.CENTER, false, banglaFont);


            // ================= BARCODE =================
            float bcX = x + 92;
            float bcY = y + 18;
            float bcW = CARD_W - 104;

            byte[] barcode = generateBarcode(safe(card.getCardNo()));
            if (barcode != null) {
                Image bc = new Image(ImageDataFactory.create(barcode));
                bc.setFixedPosition(bcX, bcY);
                bc.setWidth(bcW);
                bc.setHeight(12);
                doc.add(bc);
            }

            // ================= FOOTER TEXT (Fixed Unicode Character) =================
            String footerText = "Directorate of Women Affairs / মহিলা বিষয়ক অধিদপ্তর";
            addText(doc, footerText, x, y + 4, CARD_W, 7.0f, WHITE, TextAlignment.CENTER, false, banglaFont);

            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    // ================= HELPERS =================

    private void addLabel(Document doc, String text, float x, float y, float width, PdfFont font) {
        addText(doc, text, x, y, width, 5.5f, TEXT_MUTED, TextAlignment.LEFT, false, font);
    }

    private void addValue(Document doc, String text, float x, float y, float width, PdfFont font) {
        addText(doc, text, x, y, width, 8.5f, TEXT_DARK, TextAlignment.LEFT, false, font);
    }

    private void addText(Document doc, String text, float x, float y,
                         float width, float size, DeviceRgb color,
                         TextAlignment align, boolean bold, PdfFont font) {
        Paragraph p = new Paragraph(text)
                .setFontSize(size)
                .setFontColor(color)
                .setTextAlignment(align)
                .setFixedPosition(x, y, width);
        if (bold && font == null) p.setBold();
        if (font != null) p.setFont(font);
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

    private void addNoPhotoPlaceholder(Document doc, float x, float y, float w, float h) {
        addText(doc, "NO PHOTO", x, y + (h / 2f) - 3, w, 6f, TEXT_MUTED, TextAlignment.CENTER, false, null);
    }

    private byte[] generateBarcode(String data) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.CODE_128, 400, 50);
            BufferedImage image = new BufferedImage(400, 50, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < 400; x++) {
                for (int y = 0; y < 50; y++) {
                    image.setRGB(x, y, matrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) { return null; }
    }

    private byte[] loadBytes(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) return null;
        try {
            String cleanName = photoUrl;
            if (cleanName.contains("/")) cleanName = cleanName.substring(cleanName.lastIndexOf("/") + 1);
            if (cleanName.contains("\\")) cleanName = cleanName.substring(cleanName.lastIndexOf("\\") + 1);

            Path targetPath = Paths.get(UPLOAD_BASE).resolve(cleanName).toAbsolutePath().normalize();
            File file = targetPath.toFile();
            if (file.exists() && file.isFile()) return Files.readAllBytes(file.toPath());
        } catch (Exception ignored) {}
        return null;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private String safe(java.math.BigDecimal val) {
        return val != null ? val.toPlainString() : "০";
    }
}