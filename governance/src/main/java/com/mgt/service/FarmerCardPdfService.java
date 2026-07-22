package com.mgt.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

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

import com.mgt.model.FarmerCard;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Service
public class FarmerCardPdfService {

    // ================= TCB STYLE THEME COLORS FOR FARMER =================
    private static final DeviceRgb GOVT_GREEN = new DeviceRgb(0, 115, 75);
    private static final DeviceRgb GOVT_RED = new DeviceRgb(244, 42, 65);
    private static final DeviceRgb TEXT_DARK = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb TEXT_MUTED = new DeviceRgb(100, 100, 100);
    private static final DeviceRgb CARD_BG = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb WHITE = new DeviceRgb(255, 255, 255);
    private static final DeviceRgb BORDER_COLOR = new DeviceRgb(210, 215, 210);
    
    // Smart Card Chip Colors
    private static final DeviceRgb CHIP_GOLD = new DeviceRgb(239, 189, 73);
    private static final DeviceRgb CHIP_LINE = new DeviceRgb(170, 120, 20);

    // ================= PROFESSIONAL CR-80 CARD SIZE =================
    private static final float CARD_W = 324f; 
    private static final float CARD_H = 204f; 

    // ================= PATHS =================
    private static final String GOVT_LOGO = "src/main/resources/static/logo.png";
    private static final String FARMER_LOGO = "src/main/resources/static/farmerLogo.png"; 
    private static final String AUTH_SIGNATURE = "src/main/resources/static/signature.png";
    private static final String BANGLA_FONT = "src/main/resources/static/SolaimanLipi.ttf";

    public byte[] generateCard(FarmerCard card) {

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
                    .setFillColor(GOVT_GREEN)
                    .roundRectangle(x, y + CARD_H - 45, CARD_W, 45, 8)
                    .fill()
                    .restoreState();

            canvas.saveState()
                    .setFillColor(GOVT_GREEN)
                    .rectangle(x, y + CARD_H - 45, CARD_W, 10)
                    .fill()
                    .restoreState();

            canvas.saveState()
                    .setFillColor(GOVT_RED)
                    .rectangle(x, y + CARD_H - 47, CARD_W, 2)
                    .fill()
                    .restoreState();

            // ================= FOOTER BANNER =================
            canvas.saveState()
                    .setFillColor(GOVT_GREEN)
                    .roundRectangle(x, y, CARD_W, 16, 8)
                    .fill()
                    .restoreState();

            canvas.saveState()
                    .setFillColor(GOVT_GREEN)
                    .rectangle(x, y + 8, CARD_W, 8)
                    .fill()
                    .restoreState();

            // ================= HEADER LOGOS & TEXT =================
            float logoY = y + CARD_H - 36; 
            addImage(doc, GOVT_LOGO, x + 12, logoY, 26, 26);
            addImage(doc, FARMER_LOGO, x + CARD_W - 38, logoY, 26, 26);

            addText(doc, "Government of the People's Republic of Bangladesh", x, y + CARD_H - 12, CARD_W, 6f, WHITE, TextAlignment.CENTER, false);
            addText(doc, "Ministry of Agriculture", x, y + CARD_H - 19, CARD_W, 5.5f, WHITE, TextAlignment.CENTER, false);
            addText(doc, "SMART FARMER CARD", x, y + CARD_H - 34, CARD_W, 11f, WHITE, TextAlignment.CENTER, true);

            // ================= PHOTO (Left Panel) =================
            float photoX = x + 12;
            float photoY = y + 65; 
            float photoW = 64;
            float photoH = 76;

            canvas.saveState()
                    .setFillColor(WHITE)
                    .setStrokeColor(BORDER_COLOR)
                    .setLineWidth(0.5f)
                    .roundRectangle(photoX - 1, photoY - 1, photoW + 2, photoH + 2, 2)
                    .fillStroke()
                    .restoreState();

            // 🎯 কন্ট্রোলারের 'uploads/filename.ext' এর সাথে সামঞ্জস্যপূর্ণ বাইট রিডার
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

            // ================= ADDRESS (Below Photo) =================
            float addrY = photoY - 12;
            addText(doc, "Address:", x + 12, addrY, 70, 5.5f, TEXT_MUTED, TextAlignment.LEFT, false);
            addBanglaText(doc, safe(card.getAddress()), x + 12, addrY - 18, 75, 5.5f, TEXT_DARK, TextAlignment.LEFT, banglaFont);

            // ================= INFO FIELDS (Middle Panel) =================
            float infoX = x + 92;
            float infoY = y + 138; 
            float rowGap = 21f; 
            float midWidth = 105; 

            // 1. Name
            addLabel(doc, "নাম / Name", infoX, infoY, midWidth, banglaFont);
            addValue(doc, safe(card.getFarmerName()), infoX, infoY - 10, midWidth, banglaFont);

            // 2. Card Number
            addLabel(doc, "Card Number", infoX, infoY - rowGap, midWidth);
            addValue(doc, safe(card.getCardNo()), infoX, infoY - rowGap - 10, midWidth);

            // 3. National ID (NID)
            addLabel(doc, "National ID (NID)", infoX, infoY - (rowGap * 2), midWidth);
            addValue(doc, safe(card.getNid()), infoX, infoY - (rowGap * 2) - 10, midWidth);

            // 4. Father's Name
            addLabel(doc, "পিতার নাম / Father's Name", infoX, infoY - (rowGap * 3), midWidth, banglaFont);
            addValue(doc, safe(card.getFatherName()), infoX, infoY - (rowGap * 3) - 10, midWidth, banglaFont);

            // 5. Mobile & Land/Quota Combined (Last Row)
            float lastRowY = infoY - (rowGap * 4);
            addLabel(doc, "Mobile", infoX, lastRowY, 60);
            addValue(doc, safe(card.getContact()), infoX, lastRowY - 10, 60);

            float landX = infoX + 65;
            addLabel(doc, "Land / Quota", landX, lastRowY, 65);
            addValue(doc, safeDecimal(card.getLandTotal()) + " Acr / " + safeDecimal(card.getFertilizerQuota()), landX, lastRowY - 10, 65);

            // ================= SMART CARD CHIP =================
            float chipX = x + 202;
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
            addText(doc, "Date of Birth", qrX - 10, dobY, qrSize + 20, 5.5f, TEXT_MUTED, TextAlignment.CENTER, false);
            addText(doc, safe(card.getDateOfBirth()), qrX - 10, dobY - 8, qrSize + 20, 7.5f, TEXT_DARK, TextAlignment.CENTER, true);

            // 2. QR Code
            float qrY = y + 68; 
            canvas.saveState()
                    .setFillColor(WHITE)
                    .setStrokeColor(BORDER_COLOR)
                    .setLineWidth(0.5f)
                    .roundRectangle(qrX - 1, qrY - 1, qrSize + 2, qrSize + 2, 3)
                    .fillStroke()
                    .restoreState();

            String qrData = "EGOV_CARD|TYPE=FARMER|CARD_NO=" + safe(card.getCardNo());
            byte[] qrBytes = generateQR(qrData);
            if (qrBytes != null) {
                Image qr = new Image(ImageDataFactory.create(qrBytes));
                qr.setFixedPosition(qrX, qrY);
                qr.setWidth(qrSize);
                qr.setHeight(qrSize);
                doc.add(qr);
            }

            // 3. Authorized Signature
            float sigW = 34f; 
            float sigH = 11f; 
            float sigX = qrX + ((qrSize - sigW) / 2f); 
            float sigY = y + 42; 

            addSignatureImage(doc, card.getCertificateSignature(), AUTH_SIGNATURE, sigX, sigY + 8, sigW, sigH); 
            addText(doc, "Authorized Signature", qrX - 10, sigY, qrSize + 20, 5f, TEXT_MUTED, TextAlignment.CENTER, false);

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

            // ================= FOOTER TEXT =================
            addText(doc, "Department of Agricultural Extension (DAE)", x, y + 4, CARD_W, 7f, WHITE, TextAlignment.CENTER, false);

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

    // Overload: addLabel with optional bangla font
    private void addLabel(Document doc, String text, float x, float y, float width, PdfFont font) {
        Paragraph p = new Paragraph(text).setFontSize(6.5f).setFontColor(TEXT_MUTED).setTextAlignment(TextAlignment.LEFT).setFixedPosition(x, y, width);
        if (font != null) p.setFont(font);
        doc.add(p);
    }

    private void addValue(Document doc, String text, float x, float y, float width) {
        addText(doc, text, x, y, width, 9.0f, TEXT_DARK, TextAlignment.LEFT, true);
    }

    // Overload: addValue with optional bangla font
    private void addValue(Document doc, String text, float x, float y, float width, PdfFont font) {
        Paragraph p = new Paragraph(text).setFontSize(9.0f).setFontColor(TEXT_DARK).setTextAlignment(TextAlignment.LEFT).setFixedPosition(x, y, width).setBold();
        if (font != null) p.setFont(font);
        doc.add(p);
    }

    private void addText(Document doc, String text, float x, float y, float width, float size, DeviceRgb color, TextAlignment align, boolean bold) {
        Paragraph p = new Paragraph(text).setFontSize(size).setFontColor(color).setTextAlignment(align).setFixedPosition(x, y, width);
        if (bold) p.setBold();
        doc.add(p);
    }

    // Overload: addText with optional font
    private void addText(Document doc, String text, float x, float y, float width, float size, DeviceRgb color, TextAlignment align, boolean bold, PdfFont font) {
        Paragraph p = new Paragraph(text).setFontSize(size).setFontColor(color).setTextAlignment(align).setFixedPosition(x, y, width);
        if (bold) p.setBold();
        if (font != null) p.setFont(font);
        doc.add(p);
    }

    private void addBanglaText(Document doc, String text, float x, float y, float width, float size, DeviceRgb color, TextAlignment align, PdfFont font) {
        Paragraph p = new Paragraph(text).setFontSize(size).setFontColor(color).setTextAlignment(align).setFixedPosition(x, y, width);
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
        addText(doc, "NO PHOTO", x, y + (h / 2f) - 3, w, 6f, TEXT_MUTED, TextAlignment.CENTER, false);
    }

    private byte[] generateQR(String data) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 150, 150, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) { return null; }
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

    // 🎯 আপনার কন্ট্রোলারের আপলোড মেকানিজমের সাথে সম্পূর্ণ অ্যালাইনড মেথড
    private byte[] loadBytes(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) return null;
        
        try {
            // ১. ফাইলের নাম ক্লিন করা (উদা: "uploads/frm_photo_abc.jpg" থেকে শুধু "frm_photo_abc.jpg")
            String cleanName = photoUrl;
            if (cleanName.contains("/")) {
                cleanName = cleanName.substring(cleanName.lastIndexOf("/") + 1);
            }
            if (cleanName.contains("\\")) {
                cleanName = cleanName.substring(cleanName.lastIndexOf("\\") + 1);
            }

            // ২. কন্ট্রোলারের ডিরেক্টরি অনুযায়ী সঠিক পাথ টার্গেট করা ("src/main/resources/uploads/")
            Path targetPath = Paths.get("src/main/resources/uploads/").resolve(cleanName).toAbsolutePath().normalize();
            File file = targetPath.toFile();
            
            if (file.exists() && file.isFile()) {
                return Files.readAllBytes(file.toPath());
            }

            // ৩. সেফটি ব্যাকআপ ফলব্যাক পাথ চেক
            String fallbackPath = "src/main/resources/static/uploads/" + cleanName;
            File fallbackFile = new File(fallbackPath);
            if (fallbackFile.exists() && fallbackFile.isFile()) {
                return Files.readAllBytes(fallbackFile.toPath());
            }

        } catch (Exception ignored) {}
        return null;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }

    private String safeDecimal(java.math.BigDecimal val) {
        return val != null ? val.toPlainString() : "0";
    }
}