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

import com.mgt.model.LpgCard;

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
public class LpgCardPdfService {

    // ================= ENERGY STYLE COLORS =================
    private static final DeviceRgb GOVT_GREEN = new DeviceRgb(0, 106, 78);
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
    private static final String UPLOAD_BASE = "src/main/resources/uploads/";
    private static final String GOVT_LOGO = "src/main/resources/static/logo.png";
    private static final String LPG_LOGO = "src/main/resources/static/lpg.png"; 
    private static final String AUTH_SIGNATURE = "src/main/resources/static/signature.png";
    private static final String BANGLA_FONT = "src/main/resources/static/SolaimanLipi.ttf";

    // ================= UNICODE ESCAPE FOR PERFECT GOVERNMENT HEADER =================
    private static final String TXT_GOVT_BD = "\u0997\u09a3\u09aa\u09cd\u09b0\u099c\u09be\u09a4\u09a1\u09cd\u09aact\u09cd\u09b0\u09c0 \u09ac\u09be\u0992\u09b2\u09be\u09a6\u09c7\u09b6 \u09b8\u09b0\u0995\u09be\u09b0"; // গণপ্রজাতন্ত্রী বাংলাদেশ সরকার

    public byte[] generateCard(LpgCard card) {

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
            addImage(doc, LPG_LOGO, x + CARD_W - 38, logoY, 26, 26);

            // 🎯 ফিক্সড: বাংলা এস্কেপ সিকোয়েন্স এবং ফন্ট পাস করা হয়েছে যাতে হেডার ১০০% স্পষ্ট আসে
            addText(doc, TXT_GOVT_BD, x, y + CARD_H - 12, CARD_W, 7f, WHITE, TextAlignment.CENTER, false, banglaFont);
            addText(doc, "Energy and Mineral Resources Division", x, y + CARD_H - 19, CARD_W, 5.5f, WHITE, TextAlignment.CENTER, false, null);
            addText(doc, "SMART LPG CARD", x, y + CARD_H - 34, CARD_W, 11f, WHITE, TextAlignment.CENTER, true, null);

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

            // 🎯 ডাইনামিক আপলোড পাথ রেজোলিউশন (ছবি মিসিং হবে না)
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
            addText(doc, "Address:", x + 12, addrY, 70, 5.5f, TEXT_MUTED, TextAlignment.LEFT, false, null);
            String fullAddress = safe(card.getUpazila()); 
            addText(doc, fullAddress, x + 12, addrY - 8, 75, 5.5f, TEXT_DARK, TextAlignment.LEFT, false, null);


            // ================= MIDDLE PANEL INFO =================
            float col1X = x + 88;       
            float col2X = x + 168;      
            float infoY = y + 138; 
            float rowGap = 21f; 
            
            float col1Width = 76;
            float col2Width = 64;

            // --- Column 1: Personal Info ---
            addLabel(doc, "নাম / Cardholder's Name", col1X, infoY, col1Width, banglaFont);
            addValue(doc, safe(card.getHolderName()), col1X, infoY - 10, col1Width, banglaFont);

            addLabel(doc, "Card Number", col1X, infoY - rowGap, col1Width, null);
            addText(doc, safe(card.getCardNo()), col1X, infoY - rowGap - 10, col1Width, 9.0f, TEXT_DARK, TextAlignment.LEFT, true, null);

            addLabel(doc, "National ID (NID)", col1X, infoY - (rowGap * 2), col1Width, null);
            addText(doc, safe(card.getNid()), col1X, infoY - (rowGap * 2) - 10, col1Width, 9.0f, TEXT_DARK, TextAlignment.LEFT, true, null);

            addLabel(doc, "Mobile Number", col1X, infoY - (rowGap * 3), col1Width, null);
            addText(doc, safe(card.getContact()), col1X, infoY - (rowGap * 3) - 10, col1Width, 9.0f, TEXT_DARK, TextAlignment.LEFT, true, null);


            // --- Column 2: Quota & Dealer Info ---
            addLabel(doc, "Monthly Quota", col2X, infoY, col2Width, null);
            addValue(doc, card.getMonthlyQuota() + " Pcs", col2X, infoY - 10, col2Width, null);

            addLabel(doc, "Assigned Dealer", col2X, infoY - rowGap, col2Width, null);
            addValue(doc, safe(card.getDealerName()), col2X, infoY - rowGap - 10, col2Width, null);

            addLabel(doc, "Dealer Code", col2X, infoY - (rowGap * 2), col2Width, null);
            addText(doc, safe(card.getDealerCode()), col2X, infoY - (rowGap * 2) - 10, col2Width, 9.0f, TEXT_DARK, TextAlignment.LEFT, true, null);


            // --- Row 5 (Combined Bottom Row) ---
            float lastRowY = infoY - (rowGap * 4);
            addLabel(doc, "Cylinder Size", col1X, lastRowY, 45, null);
            addValue(doc, safe(card.getCylinderSize()), col1X, lastRowY - 10, 45, null);

            addLabel(doc, "Family Members", col1X + 50, lastRowY, 50, null);
            addText(doc, String.valueOf(card.getMembersCount()), col1X + 50, lastRowY - 10, 50, 9.0f, TEXT_DARK, TextAlignment.LEFT, true, null);


            // ================= SMART CARD CHIP =================
            float chipX = x + 215;
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


            // ================= RIGHT PANEL SIDE (DOB, QR & SIGNATURE) =================
            float qrX = x + CARD_W - 64;
            float qrSize = 52;
            
            // 1. Date of Birth
            float dobY = y + 138; 
            addText(doc, "Date of Birth", qrX - 10, dobY, qrSize + 20, 5.5f, TEXT_MUTED, TextAlignment.CENTER, false, null);
            addText(doc, safe(card.getDateOfBirth()), qrX - 10, dobY - 8, qrSize + 20, 7.5f, TEXT_DARK, TextAlignment.CENTER, true, null);

            // 2. QR Code
            float qrY = y + 68; 
            canvas.saveState()
                    .setFillColor(WHITE)
                    .setStrokeColor(BORDER_COLOR)
                    .setLineWidth(0.5f)
                    .roundRectangle(qrX - 1, qrY - 1, qrSize + 2, qrSize + 2, 3)
                    .fillStroke()
                    .restoreState();

            String qrData = "EGOV_CARD|TYPE=LPG|CARD_NO=" + safe(card.getCardNo());
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
            addText(doc, "Authorized Signature", qrX - 10, sigY, qrSize + 20, 5f, TEXT_MUTED, TextAlignment.CENTER, false, null);


            // ================= BARCODE =================
            float bcX = x + 88;
            float bcY = y + 20;
            float bcW = CARD_W - 100; 

            byte[] barcode = generateBarcode(safe(card.getCardNo()));
            if (barcode != null) {
                Image bc = new Image(ImageDataFactory.create(barcode));
                bc.setFixedPosition(bcX, bcY);
                bc.setWidth(bcW);
                bc.setHeight(12);
                doc.add(bc);
            }

            // ================= FOOTER TEXT =================
            addText(doc, "Energy & Mineral Resources Division", x, y + 4, CARD_W, 7f, WHITE, TextAlignment.CENTER, false, null);

            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return baos.toByteArray();
    }

    // ================= HELPERS (🎯 UPDATED TO ACCEPT FONTS DYNAMICALLY) =================

    private void addLabel(Document doc, String text, float x, float y, float width, PdfFont font) {
        addText(doc, text, x, y, width, 6.5f, TEXT_MUTED, TextAlignment.LEFT, false, font);
    }

    private void addValue(Document doc, String text, float x, float y, float width, PdfFont font) {
        addText(doc, text, x, y, width, 9.0f, TEXT_DARK, TextAlignment.LEFT, true, font);
    }

    private void addText(Document doc, String text, float x, float y,
                         float width, float size, DeviceRgb color,
                         TextAlignment align, boolean bold, PdfFont font) {
        Paragraph p = new Paragraph(text)
                .setFontSize(size)
                .setFontColor(color)
                .setTextAlignment(align)
                .setFixedPosition(x, y, width);
        // কাস্টম বাংলা ফন্টের অভ্যন্তরীণ ওভারল্যাপিং রুখতে সেফটি বন্ডিং
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

    // 🎯 ডাইনামিক মাল্টি-ডিরেক্টরি রুট ইমেজ রিডার (কন্ট্রোলারের সাথে সুসংগত)
    private byte[] loadBytes(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) return null;
        try {
            String cleanName = photoUrl;
            if (cleanName.contains("/")) cleanName = cleanName.substring(cleanName.lastIndexOf("/") + 1);
            if (cleanName.contains("\\")) cleanName = cleanName.substring(cleanName.lastIndexOf("\\") + 1);

            // ১. সম্ভাব্য রুট ১: src/main/resources/uploads/
            Path targetPath1 = Paths.get("src/main/resources/uploads/").resolve(cleanName).toAbsolutePath().normalize();
            File file1 = targetPath1.toFile();
            if (file1.exists() && file1.isFile()) return Files.readAllBytes(file1.toPath());

            // ২. সম্ভাব্য রুট ২: src/main/resources/static/uploads/
            Path targetPath2 = Paths.get("src/main/resources/static/uploads/").resolve(cleanName).toAbsolutePath().normalize();
            File file2 = targetPath2.toFile();
            if (file2.exists() && file2.isFile()) return Files.readAllBytes(file2.toPath());

        } catch (Exception ignored) {}
        return null;
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "—" : s;
    }
}