package com.mgt.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.*;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.*;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.*;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.*;
import com.mgt.model.EpiChild;
import com.mgt.model.EpiVaccination;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.pdf.PdfPage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Base64;

@Service
public class EpiCardPdfService {

    private static final DateTimeFormatter FMT        = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String            LOGO_PATH  = "src/main/resources/static/logo.png";
    private static final String            FONT_PATH  = "src/main/resources/static/SolaimanLipi.ttf";

    // Modern Teal & Emerald Corporate Color Palette
    private static final DeviceRgb GREEN   = new DeviceRgb(11,  75,  61);
    private static final DeviceRgb LGREEN  = new DeviceRgb(16,  125, 95);
    private static final DeviceRgb AMBER   = new DeviceRgb(217, 119, 6);
    private static final DeviceRgb BGGREEN = new DeviceRgb(240, 250, 246);
    private static final DeviceRgb BGHEAD  = new DeviceRgb(11,  75,  61);
    private static final DeviceRgb TEXT_DARK = new DeviceRgb(30, 30, 30);
    private static final DeviceRgb GRAY    = new DeviceRgb(75,  85,  99);
    private static final DeviceRgb LGRAY   = new DeviceRgb(241, 245, 249);
    private static final DeviceRgb BORDER_G = new DeviceRgb(209, 235, 224);
    private static final DeviceRgb ALTROW  = new DeviceRgb(248, 250, 252);
    private static final DeviceRgb WHITE   = new DeviceRgb(255, 255, 255);

    @Value("${citizen.upload.dir:src/main/resources/uploads/}")
    private String uploadDir;

    public byte[] generate(EpiChild child, List<EpiVaccination> vaccinations) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);

            PdfPage page = pdf.addNewPage(PageSize.A4);

            Document doc = new Document(pdf, PageSize.A4);
            doc.setMargins(40, 40, 40, 40);

            // ── Bangla font ──────────────────────────────────────
            PdfFont banglaFont = null;
            File fontFile = new File(FONT_PATH);
            if (fontFile.exists()) {
                banglaFont = PdfFontFactory.createFont(
                        FONT_PATH,
                        PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
                );
                doc.setFont(banglaFont);
            }

            float W = PageSize.A4.getWidth();
            float H = PageSize.A4.getHeight();

            // ── Premium Outer Borders ─────────────────────────────
            PdfCanvas canvas = new PdfCanvas(page);
            canvas.setStrokeColor(GREEN)
                  .setLineWidth(2.0f)
                  .rectangle(20, 20, W - 40, H - 40)
                  .stroke();

            canvas.setStrokeColor(AMBER)
                  .setLineWidth(0.8f)
                  .rectangle(24, 24, W - 48, H - 48)
                  .stroke();
            canvas.release();

            // ── Header Layout (Logo Left, Title Center-Right) ─────
            Table headerTable = new Table(new float[]{15, 85}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);
            
            File logoFile = new File(LOGO_PATH);
            if (logoFile.exists()) {
                try {
                    byte[] logoBytes = java.nio.file.Files.readAllBytes(logoFile.toPath());
                    Image img = new Image(ImageDataFactory.create(logoBytes)).setWidth(52);
                    headerTable.addCell(new Cell().add(img).setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE));
                    
                    // Watermark (Centered Background)
                    Image wm = new Image(ImageDataFactory.create(logoBytes))
                               .setFixedPosition((W / 2) - 125, (H / 2) - 125)
                               .setWidth(250).setOpacity(0.04f);
                    doc.add(wm);
                } catch (Exception e) {
                    headerTable.addCell(new Cell().setBorder(null));
                }
            } else {
                headerTable.addCell(new Cell().setBorder(null));
            }

            // Typography Container
            Cell titlesCell = new Cell().setBorder(null).setVerticalAlignment(VerticalAlignment.MIDDLE).setPaddingLeft(10);
            titlesCell.add(new Paragraph("Government of the People's Republic of Bangladesh").setFontSize(10).setBold().setFontColor(GREEN).setMargin(0));
            titlesCell.add(new Paragraph("Health Services Division — EPI Vaccination Programme").setFontSize(14).setBold().setFontColor(LGREEN).setMargin(0).setMarginTop(2));
            titlesCell.add(new Paragraph("EXPANDED PROGRAMME ON IMMUNIZATION — CHILD VACCINATION CARD").setFontSize(8.5f).setFontColor(GRAY).setMargin(0).setMarginTop(1));
            headerTable.addCell(titlesCell);
            doc.add(headerTable);

            // ── Modern Card Badge / Banner ────────────────────────
            Table banner = new Table(new float[]{42, 38, 20}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);
            
            Cell cardNoCell = new Cell()
                    .add(new Paragraph("CARD NO:  " + safe(child.getCardNo())).setBold().setFontSize(12).setFontColor(GREEN))
                    .setBackgroundColor(BGGREEN).setPadding(10)
                    .setBorder(new SolidBorder(BORDER_G, 1f))
                    .setBorderRight(null);
            if (banglaFont != null) cardNoCell.setFont(banglaFont);
            banner.addCell(cardNoCell);

            Cell statusCell = new Cell()
                    .add(new Paragraph("Issue Date: " + LocalDate.now().format(FMT)).setFontSize(8.5f).setFontColor(GRAY))
                    .add(new Paragraph("Status: " + safe(child.getStatus())).setFontSize(10).setBold().setFontColor(AMBER))
                    .setBackgroundColor(BGGREEN).setPadding(8).setPaddingRight(12)
                    .setBorder(new SolidBorder(BORDER_G, 1f))
                    .setBorderLeft(null)
                    .setTextAlignment(TextAlignment.RIGHT);
            if (banglaFont != null) statusCell.setFont(banglaFont);
            banner.addCell(statusCell);

            Cell qrCell = new Cell()
                    .setBackgroundColor(BGGREEN).setPadding(5)
                    .setBorder(new SolidBorder(BORDER_G, 1f))
                    .setTextAlignment(TextAlignment.CENTER);
            try {
                byte[] qrBytes = generateQr("EPI:" + safe(child.getCardNo()));
                Image qr = new Image(ImageDataFactory.create(qrBytes)).setWidth(62).setHeight(62);
                qrCell.add(qr);
                qrCell.add(new Paragraph("SCAN TO UPDATE DOSE").setFontSize(5.5f).setBold().setFontColor(GREEN));
            } catch (Exception qrEx) {
                qrCell.add(new Paragraph("QR unavailable").setFontSize(7f).setFontColor(GRAY));
            }
            banner.addCell(qrCell);
            doc.add(banner);

            // ── Child Info Section (No Background, Title with Colon) ──
            addSectionTitle(doc, "Child & Guardian Information", banglaFont);
            
            Table infoWrap = new Table(new float[]{80, 20}).setWidth(UnitValue.createPercentValue(100)).setMarginBottom(15);
            Table infoGrid = new Table(new float[]{20, 30, 20, 30}).setWidth(UnitValue.createPercentValue(100));
            
            addGridRow(infoGrid, "Child Name", safe(child.getChildName()), "Date of Birth", fmt(child.getDateOfBirth()), banglaFont);
            addGridRow(infoGrid, "Gender", safe(child.getGender()), "Guardian Phone", safe(child.getGuardianPhone()), banglaFont);
            addGridRow(infoGrid, "Father's Name", safe(child.getFatherName()), "Mother's Name", safe(child.getMotherName()), banglaFont);
            addGridRow(infoGrid, "Ward", safe(child.getWard()), "Upazila", safe(child.getUpazila()), banglaFont);
            
            // Last line spanning with District (No Background, Border Only)
            Cell distLabel = new Cell().add(new Paragraph("District:").setBold().setFontSize(8.5f).setFontColor(GREEN)).setPadding(5).setBorder(new SolidBorder(LGRAY, 0.5f));
            Cell distVal = new Cell(1, 3).add(new Paragraph(safe(child.getDistrict())).setFontSize(8.5f)).setPadding(5).setBorder(new SolidBorder(LGRAY, 0.5f));
            if (banglaFont != null) { distLabel.setFont(fontFile.exists() ? banglaFont : null); distVal.setFont(banglaFont); }
            infoGrid.addCell(distLabel).addCell(distVal);
            
            infoWrap.addCell(new Cell().add(infoGrid).setBorder(null).setPadding(0));
            infoWrap.addCell(photoCell(child));
            doc.add(infoWrap);

            // ── Vaccination Schedule Table ─────────────────────────────
            addSectionTitle(doc, "Vaccination Schedule", banglaFont);

            Table schedule = new Table(new float[]{5, 23, 10, 17, 17, 16, 12})
                              .setWidth(UnitValue.createPercentValue(100))
                              .setMarginBottom(20);

            String[] headers = { "#", "Vaccine Name", "Dose", "Scheduled Date", "Given Date", "Health Center", "Status" };
            for (String h : headers) {
                Cell hCell = new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(8f).setFontColor(WHITE))
                        .setBackgroundColor(BGHEAD).setPadding(7).setBorder(new SolidBorder(GREEN, 0.5f));
                if (banglaFont != null) hCell.setFont(banglaFont);
                schedule.addHeaderCell(hCell);
            }

            int i = 1;
            if (vaccinations != null) {
                for (EpiVaccination v : vaccinations) {
                    DeviceRgb rowBg = (i % 2 == 0) ? ALTROW : WHITE;
                    DeviceRgb statusColor = statusColor(v.getStatus());

                    schedule.addCell(dataCell(String.valueOf(i++), rowBg, banglaFont));
                    schedule.addCell(dataCell(safe(v.getVaccineName()), rowBg, banglaFont));
                    schedule.addCell(dataCell(safe(v.getDoseNo()), rowBg, banglaFont));
                    schedule.addCell(dataCell(fmt(v.getScheduledDate()), rowBg, banglaFont));
                    schedule.addCell(dataCell(fmt(v.getGivenDate()), rowBg, banglaFont));
                    schedule.addCell(dataCell(safe(v.getHealthCenter()), rowBg, banglaFont));

                    Cell statusCell1 = new Cell()
                            .add(new Paragraph(safe(v.getStatus())).setBold().setFontSize(8f).setFontColor(statusColor))
                            .setBackgroundColor(rowBg).setPadding(6)
                            .setBorder(new SolidBorder(LGRAY, 0.5f));
                    if (banglaFont != null) statusCell1.setFont(banglaFont);
                    schedule.addCell(statusCell1);
                }
            }
            doc.add(schedule);

            // ── Signature + Seal ───────────────────────────────────
            String finalSignature = hasText(child.getSecondSignature()) ? child.getSecondSignature() : child.getAuthoritySignature();
            String finalSeal = hasText(child.getSecondSeal()) ? child.getSecondSeal() : child.getAuthoritySeal();
            Table sign = new Table(new float[]{50, 50}).setWidth(UnitValue.createPercentValue(100)).setMarginTop(25);
            sign.addCell(signatureCell(child.getFirstSignature(), child.getFirstSeal(), "First Approving Officer", false, GREEN, GRAY));
            sign.addCell(signatureCell(finalSignature, finalSeal, "Final Approving Officer", true, GREEN, GRAY));
            doc.add(sign);

            // ── Footer Copyright ──────────────────────────────────────
            doc.add(new Paragraph("EPI Card | Health Department | Government of the People's Republic of Bangladesh")
                    .setTextAlignment(TextAlignment.CENTER).setFontSize(8f)
                    .setFontColor(GRAY).setMarginTop(25));

            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate EPI PDF", e);
        }
        return out.toByteArray();
    }


    private Cell signatureCell(String signatureUrl, String sealUrl, String role, boolean right, DeviceRgb roleColor, DeviceRgb lineColor) {
        Cell cell = new Cell().setBorder(null).setMinHeight(120).setPaddingLeft(right ? 0 : 10).setPaddingRight(right ? 10 : 0);
        try {
            if (signatureUrl != null && !signatureUrl.isBlank()) {
                Image sig = new Image(ImageDataFactory.create(decodeDataUrl(signatureUrl))).setWidth(112).setHeight(34);
                sig.setHorizontalAlignment(right ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
                cell.add(sig);
            } else {
                cell.add(new Paragraph("__________________________").setFontSize(9).setFontColor(lineColor)
                        .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
            }
        } catch (Exception ex) {
            cell.add(new Paragraph("__________________________").setFontSize(9).setFontColor(lineColor)
                    .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
        }
        cell.add(new Paragraph(role).setFontSize(8.5f).setBold().setFontColor(roleColor).setMarginTop(3)
                .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
        if (sealUrl != null && !sealUrl.isBlank()) {
            try {
                Image seal = new Image(ImageDataFactory.create(decodeDataUrl(sealUrl))).setWidth(98).setHeight(52);
                seal.setHorizontalAlignment(right ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT);
                cell.add(seal);
            } catch (Exception ex) {
                cell.add(new Paragraph("INVALID SEAL").setFontSize(7).setBold().setFontColor(ColorConstants.RED)
                        .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
            }
        } else if (signatureUrl != null && !signatureUrl.isBlank()) {
            cell.add(new Paragraph("SEAL MISSING").setFontSize(7).setBold().setFontColor(ColorConstants.RED)
                    .setTextAlignment(right ? TextAlignment.RIGHT : TextAlignment.LEFT));
        }
        return cell;
    }

    private Cell photoCell(EpiChild child) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(BORDER_G, 0.8f))
                .setBackgroundColor(ALTROW)
                .setPadding(6)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        try {
            byte[] bytes = loadUploadedBytes(child.getChildPhotoUrl());
            if (bytes != null) {
                Image photo = new Image(ImageDataFactory.create(bytes))
                        .setWidth(78)
                        .setHeight(92);
                photo.setHorizontalAlignment(HorizontalAlignment.CENTER);
                cell.add(photo);
            } else {
                cell.add(new Paragraph("PHOTO").setBold().setFontSize(9).setFontColor(GRAY));
            }
        } catch (Exception ex) {
            cell.add(new Paragraph("PHOTO").setBold().setFontSize(9).setFontColor(GRAY));
        }
        cell.add(new Paragraph("Child Photo").setFontSize(6.5f).setFontColor(GRAY).setMarginTop(4));
        return cell;
    }

    private byte[] loadUploadedBytes(String path) {
        if (path == null || path.isBlank()) return null;
        try {
            if (path.startsWith("data:")) return decodeDataUrl(path);
            String name = path.startsWith("uploads/") ? path.substring("uploads/".length()) : path;
            File file = new File(uploadDir, name);
            if (!file.exists()) file = new File("src/main/resources/uploads", name);
            if (!file.exists()) file = new File(path);
            return file.exists() ? java.nio.file.Files.readAllBytes(file.toPath()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] decodeDataUrl(String dataUrl) {
        String raw = dataUrl != null && dataUrl.contains(",") ? dataUrl.substring(dataUrl.indexOf(',') + 1) : dataUrl;
        return Base64.getDecoder().decode(raw);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // ── Helper Methods for Grid Structure ─────────────────────────────

    private void addSectionTitle(Document doc, String text, PdfFont font) {
        Paragraph p = new Paragraph(text).setBold().setFontSize(11f).setFontColor(GREEN)
                       .setMarginTop(12).setMarginBottom(6)
                       .setBorderBottom(new SolidBorder(LGREEN, 1.2f));
        if (font != null) p.setFont(font);
        doc.add(p);
    }

    // 🎯 ফিক্সড গ্রিড রো: ব্যাকগ্রাউন্ড কালার রিমুভ করা হয়েছে এবং টাইটেলের পর কোলন (:) যুক্ত করা হয়েছে
    private void addGridRow(Table table, String f1Label, String f1Val, String f2Label, String f2Val, PdfFont font) {
        Cell l1 = new Cell().add(new Paragraph(f1Label + ":").setBold().setFontSize(8.5f).setFontColor(GREEN)).setPadding(5).setBorder(new SolidBorder(LGRAY, 0.5f));
        Cell v1 = new Cell().add(new Paragraph(f1Val.isBlank() ? "—" : f1Val).setFontSize(8.5f)).setPadding(5).setBorder(new SolidBorder(LGRAY, 0.5f));
        Cell l2 = new Cell().add(new Paragraph(f2Label + ":").setBold().setFontSize(8.5f).setFontColor(GREEN)).setPadding(5).setBorder(new SolidBorder(LGRAY, 0.5f));
        Cell v2 = new Cell().add(new Paragraph(f2Val.isBlank() ? "—" : f2Val).setFontSize(8.5f)).setPadding(5).setBorder(new SolidBorder(LGRAY, 0.5f));
        
        if (font != null) {
            l1.setFont(font); v1.setFont(font);
            l2.setFont(font); v2.setFont(font);
        }
        table.addCell(l1).addCell(v1).addCell(l2).addCell(v2);
    }

    private Cell dataCell(String text, DeviceRgb bg, PdfFont font) {
        Cell c = new Cell()
                .add(new Paragraph(text == null || text.isBlank() ? "—" : text).setFontSize(8f).setFontColor(TEXT_DARK))
                .setBackgroundColor(bg).setPadding(6)
                .setBorder(new SolidBorder(LGRAY, 0.5f));
        if (font != null) c.setFont(font);
        return c;
    }

    private DeviceRgb statusColor(String status) {
        if (status == null) return GRAY;
        
        switch (status.trim()) {
            case "Given":
                return new DeviceRgb(16, 125, 95);
            case "Due":
                return new DeviceRgb(217, 119, 6);
            case "Missed":
                return new DeviceRgb(220, 38, 38);
            case "Scheduled":
                return new DeviceRgb(37, 99, 235);
            default:
                return GRAY;
        }
    }

    private byte[] generateQr(String value) throws Exception {
        BitMatrix matrix = new QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, 180, 180);
        ByteArrayOutputStream qrOut = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", qrOut);
        return qrOut.toByteArray();
    }

    private String fmt(LocalDate d) {
        return d != null ? d.format(FMT) : "—";
    }

    private String safe(Object v) {
        return v == null ? "" : v.toString();
    }
}
