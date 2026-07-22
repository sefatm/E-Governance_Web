package com.mgt.service;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.mgt.model.TradeLicenseApply;
import com.mgt.model.TradeRenewal;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class TradeLicensePdfService {

    // Colours
    private static final DeviceRgb C_DKGREEN = new DeviceRgb(0,   80,  0);
    private static final DeviceRgb C_GREEN   = new DeviceRgb(0,  120,  0);
    private static final DeviceRgb C_RED     = new DeviceRgb(185,  0,  0);
    private static final DeviceRgb C_NAVY    = new DeviceRgb(0,   40, 90);
    private static final DeviceRgb C_GOLD    = new DeviceRgb(180, 140,  0);
    private static final DeviceRgb C_GRAY    = new DeviceRgb(100, 100, 100);

    // colours for tables
    private static final DeviceRgb C_LABEL   = new DeviceRgb(30,  30,  30);  // Dark label
    private static final DeviceRgb C_VALUE   = new DeviceRgb(20,  20,  20);  // Dark value
    private static final DeviceRgb C_ALT_BG  = new DeviceRgb(235, 242, 228); // Subtle green alt row
    private static final DeviceRgb C_BORDER  = new DeviceRgb(190, 205, 190); // Subtle cell border
    private static final DeviceRgb C_TEXT    = new DeviceRgb(30,  30,  30);  // Default body text

    // Cached Logo
    private byte[] cachedGovtLogo = null;

    @PostConstruct
    private void init() {
        loadGovtLogo();
    }

    private void loadGovtLogo() {
        try {
            String[] paths = {
                "static/logo.png",
                "images/logo.png",
                "logo.png"
            };
            for (String path : paths) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(path);
                if (is != null) {
                    cachedGovtLogo = is.readAllBytes();
                    is.close();
                    System.out.println("✅ Govt logo loaded from: " + path + " (" + cachedGovtLogo.length + " bytes)");
                    return;
                }
            }
            String absPath = System.getProperty("user.dir") + "/src/main/resources/static/logo.png";
            java.io.File f = new java.io.File(absPath);
            if (f.exists()) {
                java.io.FileInputStream fis = new java.io.FileInputStream(f);
                cachedGovtLogo = fis.readAllBytes();
                fis.close();
                System.out.println("✅ Govt logo loaded from absolute path: " + absPath);
                return;
            }
            System.out.println("⚠️ logo.png not found. Using programmatic fallback.");
        } catch (Exception e) {
            System.err.println("⚠️ Error loading logo: " + e.getMessage());
        }
    }

    private Image getGovtLogoImage(float width, float height) throws Exception {
        byte[] logoBytes = cachedGovtLogo;
        if (logoBytes == null || logoBytes.length < 100) {
            logoBytes = makeMonogram(150);
        }
        Image img = new Image(ImageDataFactory.create(logoBytes));
        img.setWidth(width);
        img.setHeight(height);
        return img;
    }

    //  TRADE LICENSE CERTIFICATE
    public byte[] generateTradeLicenseCertificate(TradeLicenseApply t) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(out));
            Document    doc = new Document(pdf, PageSize.A4);
            doc.setMargins(25, 40, 25, 40);

            float W = PageSize.A4.getWidth();
            float H = PageSize.A4.getHeight();

            PdfPage pg = pdf.addNewPage();
            PdfCanvas cv = new PdfCanvas(pg);
            drawPageBorder(cv, W, H, C_GREEN);
            drawLogoWatermark(cv, W, H);
            cv.release();

            // HEADER SECTION 
            Table hdr = pct(new float[]{15f, 70f, 15f}).setMarginBottom(4);

            byte[] qrBytes = makeQR("https://municipality.gov.bd/verify/" + v(t.getLicenseNumber()), 75);
            hdr.addCell(new Cell()
                .add(new Image(ImageDataFactory.create(qrBytes)).setWidth(62).setHeight(62))
                .setPadding(2));

            Cell hdrMid = new Cell()
                .add(p("Government of the People's Republic of Bangladesh", 12f, true, C_DKGREEN).setMarginBottom(1))
                .add(p("Office of the Registrar, Trade License Division", 9f, false, C_TEXT).setMarginBottom(1))
                .add(p("Madaripur Municipality, Bangladesh", 9f, false, C_TEXT).setMarginBottom(4))
                .add(p("TRADE LICENSE CERTIFICATE", 15f, true, C_RED).setMarginBottom(1))
                .add(p("(Under Local Government (City Corporation and Municipality) Act, 2009)", 7.5f, false, C_GRAY))
                .setTextAlignment(TextAlignment.CENTER);
            hdr.addCell(hdrMid);

            hdr.addCell(new Cell()
                .add(getGovtLogoImage(70, 70))
                .setPadding(2).setTextAlignment(TextAlignment.RIGHT));

            doc.add(hdr);

            doc.add(p("─────────────────────────────────────────────────────────────────────", 7f, false, C_GREEN).setMarginBottom(4));

            // LICENSE NUMBER BAR
            Table numBar = pct(new float[]{45f, 55f}).setMarginBottom(8);
            numBar.addCell(new Cell()
                .add(p("LICENSE NUMBER", 9f, true, C_LABEL).setMarginBottom(2))
                .add(p(v(t.getLicenseNumber()), 14f, true, C_RED))
                .setBackgroundColor(new DeviceRgb(255, 246, 246))
                .setPadding(10));
            numBar.addCell(new Cell()
                .add(p("Date of Issue   :  " + now(),                    9f, false, C_TEXT))
                .add(p("Valid Until        :  " + exp(t.getLicensePeriod()), 9f, false, C_TEXT))
                .add(p("Applied Date   :  " + v(t.getAppliedDate()),     9f, false, C_TEXT))
                .add(p("Status               :  " + v(t.getStatus()),     9f, true, C_DKGREEN))
                .setPadding(10));
            doc.add(numBar);

            // BUSINESS INFORMATION
            doc.add(secHead("  BUSINESS INFORMATION", C_DKGREEN));
            Table biz = pct(new float[]{38f, 62f}).setMarginBottom(5);
            row(biz, "Business Name",     v(t.getBusinessName()),     false);
            row(biz, "Business Type",     v(t.getBusinessType()),     true);
            row(biz, "License Period",    v(t.getLicensePeriod()) + " Year(s)", false);
            row(biz, "Business Address",  v(t.getAddress()),          true);
            row(biz, "Ward No.",          v(t.getWardNo()),           true);
            row(biz, "Holding No.",       v(t.getHoldingNo()),        true);
            doc.add(biz);

            // OWNER INFORMATION 
            doc.add(secHead("  OWNER INFORMATION", C_DKGREEN));
            Table own = pct(new float[]{38f, 62f}).setMarginBottom(8);
            row(own, "Owner's Name",   v(t.getOwnerName()),   true);
            row(own, "Father's Name",  v(t.getFatherName()),  true);
            row(own, "Mother's Name",  v(t.getMotherName()),  true);
            row(own, "Date of Birth",  v(t.getDateOfBirth()), true);
            row(own, "NID Number",     v(t.getNid()),         false);
            row(own, "Mobile Number",  v(t.getMobile()),      true);
            row(own, "Email Address",  v(t.getEmail()),       false);
            doc.add(own);

            // DECLARATION
            doc.add(new Paragraph(
                "This is to certify that the above-mentioned business has been duly registered " +
                "under Madaripur Municipality in accordance with the Local Government Act. " +
                "The licensee is authorized to conduct the stated business activities " +
                "within the jurisdiction of Madaripur Municipality for the period specified above.")
                .setFontSize(8.5f).setFontColor(C_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setPaddingLeft(15).setPaddingRight(15)
                .setMarginBottom(8).setItalic());

            addTradeSignatureImages(doc, t.getFirstSignature(), t.getSecondSignature(), "Trade License Officer");

            doc.add(p("─────────────────────────────────────────────────────────────────────", 7f, false, C_GREEN));
            doc.add(p("Verify online: municipality.gov.bd  |  Scan QR Code  |  License No: " + v(t.getLicenseNumber()),
                7f, false, C_GRAY).setTextAlignment(TextAlignment.CENTER));

            doc.close();

        } catch (Exception e) {
            System.err.println(">>> TradeLicense PDF ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    
    //  RENEWAL CERTIFICATE
    public byte[] generateRenewalCertificate(TradeRenewal r) {
    	
        TradeLicenseApply license = r.getOriginalLicense();
        
        if (license == null) {
            throw new RuntimeException("License data missing. Please re-save renewal.");
        }

        String licenseNo = license.getLicenseNumber();
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfDocument pdf = new PdfDocument(new PdfWriter(out));
            Document    doc = new Document(pdf, PageSize.A4);
            doc.setMargins(25, 40, 25, 40);

            float W = PageSize.A4.getWidth();
            float H = PageSize.A4.getHeight();

            PdfPage pg = pdf.addNewPage();
            PdfCanvas cv = new PdfCanvas(pg);
            drawPageBorder(cv, W, H, C_NAVY);
            drawLogoWatermark(cv, W, H);
            cv.release();

            Table hdr = pct(new float[]{15f, 70f, 15f}).setMarginBottom(4);

            byte[] qrBytes = makeQR("https://municipality.gov.bd/verify/" + v(r.getOriginalLicense().getLicenseNumber()), 75);
            hdr.addCell(new Cell()
                .add(new Image(ImageDataFactory.create(qrBytes)).setWidth(62).setHeight(62))
                .setPadding(2));

            Cell hdrMid = new Cell()
                .add(p("Government of the People's Republic of Bangladesh", 12f, true, C_DKGREEN).setMarginBottom(1))
                .add(p("Office of the Registrar, Trade License Division", 9f, false, C_TEXT).setMarginBottom(1))
                .add(p("Madaripur Municipality, Bangladesh", 9f, false, C_TEXT).setMarginBottom(4))
                .add(p("TRADE LICENSE RENEWAL CERTIFICATE", 14f, true, C_NAVY).setMarginBottom(1))
                .add(p("(Renewal under Local Government Act, 2009)", 7.5f, false, C_GRAY))
                .setTextAlignment(TextAlignment.CENTER);
            hdr.addCell(hdrMid);

            hdr.addCell(new Cell()
                .add(getGovtLogoImage(70, 70))
                .setPadding(2).setTextAlignment(TextAlignment.RIGHT));
            doc.add(hdr);

            doc.add(p("─────────────────────────────────────────────────────────────────────", 7f, false, C_NAVY).setMarginBottom(4));

            Table numBar = pct(new float[]{45f, 55f}).setMarginBottom(8);
            numBar.addCell(new Cell()
                .add(p("LICENSE NUMBER", 9f, true, C_LABEL).setMarginBottom(2))
                .add(p(v(r.getOriginalLicense().getLicenseNumber()), 14f, true, C_NAVY))
                .setBackgroundColor(new DeviceRgb(245, 247, 255)).setPadding(10));
            numBar.addCell(new Cell()
                .add(p("Renewal Date   :  " + now(),                       9f, false, C_TEXT))
                .add(p("New Expiry       :  " + expYr(r.getRenewalPeriod()), 9f, false, C_TEXT))
                .add(p("Prev. Expiry     :  " + v(r.getLicenseExpiry()),     9f, false, C_TEXT))
                .add(p("Status               :  " + v(r.getStatus()),       9f, true, C_DKGREEN))
                .setPadding(10));
            doc.add(numBar);

            doc.add(secHead("  APPLICANT INFORMATION", C_NAVY));
            Table app = pct(new float[]{38f, 62f}).setMarginBottom(5);
            row(app, "Applicant's Name", v(r.getApplicantName()), false);
            row(app, "Father's Name",    v(r.getFatherName()),    true);
            row(app, "Mother's Name",    v(r.getMotherName()),    false);
            row(app, "Date of Birth",    v(r.getDateOfBirth()),   true);
            row(app, "NID Number",       v(r.getNid()),           false);
            row(app, "Contact",          v(r.getContact()),       true);
            row(app, "Email",            v(r.getEmail()),         true);
            doc.add(app);

            doc.add(secHead("  BUSINESS & RENEWAL DETAILS", C_NAVY));
            Table biz = pct(new float[]{38f, 62f}).setMarginBottom(8);
            row(biz, "Business Name",     v(r.getBusinessName()),      true);
            row(biz, "Business Type",     v(r.getBusinessType()),      true);
            row(biz, "Ward No.",          v(r.getWardNo()),            false);
            row(biz, "Holding No.",       v(r.getHoldingNo()),         true);
            row(biz, "Issuing Authority", v(r.getIssuingAuthority()),  false);
            //row(biz, "Annual Income",     "Tk. " + v(r.getAnnualIncome()), true);
            //row(biz, "Tax Paid",          "Tk. " + v(r.getTaxPaid()),  false);
            row(biz, "Renewal Period",    r.getRenewalPeriod() + " Year(s)", true);
            row(biz, "Purpose",           v(r.getPurpose()),           false);
            doc.add(biz);

            doc.add(new Paragraph(
                "This certificate confirms that the above trade license has been duly renewed " +
                "under Madaripur Municipality. The licensee is authorized to continue business " +
                "activities for the renewed period stated herein.")
                .setFontSize(8.5f).setFontColor(C_TEXT)
                .setTextAlignment(TextAlignment.CENTER)
                .setPaddingLeft(15).setPaddingRight(15)
                .setMarginBottom(8).setItalic());

            addTradeSignatureImages(doc, r.getFirstSignature(), r.getSecondSignature(), "Renewal Authority");

            doc.add(p("─────────────────────────────────────────────────────────────────────", 7f, false, C_NAVY));
            doc.add(p("Verify online: municipality.gov.bd  |  Scan QR Code  |  License No: " + v(r.getOriginalLicense().getLicenseNumber()),
                7f, false, C_GRAY).setTextAlignment(TextAlignment.CENTER));

            doc.close();

        } catch (Exception e) {
            System.err.println(">>> Renewal PDF ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        return out.toByteArray();
    }

    //  MONOGRAM
    private byte[] makeMonogram(int size) throws Exception {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D    g   = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, size, size);
        g.setComposite(AlphaComposite.SrcOver);

        int cx = size / 2, cy = size / 2, r = size / 2 - 2;

        g.setColor(new Color(0, 80, 0));
        g.setStroke(new BasicStroke(2.5f));
        g.drawOval(cx - r, cy - r, r * 2, r * 2);

        int r2 = r - 6;
        g.setStroke(new BasicStroke(1f));
        g.drawOval(cx - r2, cy - r2, r2 * 2, r2 * 2);

        g.setColor(new Color(0, 80, 0));
        g.fillOval(cx - 6, cy - 8, 12, 16);

        int[] lx = {cx - 6, cx - 26, cx - 20, cx - 8};
        int[] ly = {cy - 4, cy - 14, cy + 2,  cy + 4};
        g.fillPolygon(lx, ly, 4);

        int[] rx = {cx + 6, cx + 26, cx + 20, cx + 8};
        int[] ry = {cy - 4, cy - 14, cy + 2,  cy + 4};
        g.fillPolygon(rx, ry, 4);

        g.fillOval(cx - 5, cy - 16, 10, 10);

        int[] bx = {cx - 2, cx + 2, cx + 5};
        int[] by = {cy - 8, cy - 8, cy - 4};
        g.fillPolygon(bx, by, 3);

        g.setStroke(new BasicStroke(1.2f));
        for (int i = -1; i <= 1; i++) {
            int wcy = cy + 14 + i * 3;
            g.drawArc(cx - 14, wcy - 4, 28, 8, 0, 180);
        }

        g.setFont(new Font("SansSerif", Font.BOLD, 7));
        g.setColor(new Color(0, 80, 0));
        String txt = "MADARIPUR  MUNICIPALITY";
        double angle = -Math.PI * 0.7;
        double step  = Math.PI * 1.4 / (txt.length() - 1);
        int    tr    = r - 3;
        for (int i = 0; i < txt.length(); i++) {
            int tx = (int)(cx + tr * Math.cos(angle)) - 3;
            int ty = (int)(cy + tr * Math.sin(angle)) + 3;
            g.drawString(String.valueOf(txt.charAt(i)), tx, ty);
            angle += step;
        }

        g.dispose();
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ImageIO.write(img, "png", o);
        return o.toByteArray();
    }

    //  HELPERS
    private void drawPageBorder(PdfCanvas cv, float W, float H, DeviceRgb color) {
        cv.setStrokeColor(color).setLineWidth(3f)
          .rectangle(10, 10, W - 20, H - 20).stroke();
        cv.setStrokeColor(color).setLineWidth(0.7f)
          .rectangle(16, 16, W - 32, H - 32).stroke();
    }

    // Watermark
    private void drawLogoWatermark(PdfCanvas cv, float W, float H) {
        try {
            byte[] logoBytes = (cachedGovtLogo != null && cachedGovtLogo.length > 100) ? cachedGovtLogo : makeMonogram(250);
            ImageData imageData = ImageDataFactory.create(logoBytes);

            float imgSize = 180f;
            float x = (W - imgSize) / 2f;
            float y = (H - imgSize) / 2f;

            cv.saveState();
            cv.setExtGState(new PdfExtGState().setFillOpacity(0.07f));
            cv.addImageFittedIntoRectangle(imageData, new com.itextpdf.kernel.geom.Rectangle(x, y, imgSize, imgSize), false);
            
            cv.restoreState();
        } catch (Exception e) {
            cv.setFillColor(new DeviceRgb(220, 242, 220)).circle(W / 2f, H / 2f, 108f).fill();
        }
    }

    private Table pct(float[] cols) {
        return new Table(UnitValue.createPercentArray(cols))
                .setWidth(UnitValue.createPercentValue(100));
    }

    private Paragraph p(String text, float size, boolean bold, DeviceRgb col) {
        Paragraph pg = new Paragraph(text).setFontSize(size).setMarginBottom(2);
        if (bold) pg.setBold();
        pg.setFontColor(col != null ? col : C_TEXT);
        return pg;
    }

    private Paragraph secHead(String title, DeviceRgb bg) {
        return new Paragraph(title)
                .setFontSize(9.5f).setBold()
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(bg)
                .setPaddingTop(5).setPaddingBottom(5).setMarginBottom(0);
    }

    private void row(Table t, String label, String val, boolean alt) {
        Cell lc = new Cell()
                .add(new Paragraph(label)
                .setBold()
                .setFontSize(9f)
                .setFontColor(C_LABEL))
                .setPaddingLeft(12).setPaddingTop(6).setPaddingBottom(6)
                .setBorder(new SolidBorder(C_BORDER, 0.5f));

        Cell vc = new Cell()
                .add(new Paragraph((val == null || val.isBlank()) ? "—" : val)
                .setFontSize(9f)
                .setFontColor(C_VALUE))
                .setPaddingLeft(12).setPaddingTop(6).setPaddingBottom(6)
                .setBorder(new SolidBorder(C_BORDER, 0.5f));

        if (alt) {
            lc.setBackgroundColor(C_ALT_BG);
            vc.setBackgroundColor(C_ALT_BG);
        }

        t.addCell(lc);
        t.addCell(vc);
    }

    private void addSignatures(Document doc, String middleTitle) {
        Table s = pct(new float[]{34f, 32f, 34f}).setMarginTop(8);

        s.addCell(new Cell()
            .add(p("\n\n____________________", 10f, false, C_TEXT))
            .add(p("Seal & Signature", 8.5f, true, C_LABEL))
            .add(p("Assistant to Registrar", 8f, false, C_GRAY))
            .add(p("(Preparation, Verification)", 7.5f, false, C_GRAY)));

        s.addCell(new Cell()
            .add(p("[ OFFICIAL SEAL ]", 8f, false, C_GRAY)
                .setTextAlignment(TextAlignment.CENTER))
            .add(p(middleTitle, 7.5f, false, C_GRAY)
                .setTextAlignment(TextAlignment.CENTER))
            .setVerticalAlignment(VerticalAlignment.MIDDLE));

        s.addCell(new Cell()
            .add(p("\n\n____________________", 10f, false, C_TEXT).setTextAlignment(TextAlignment.RIGHT))
            .add(p("Seal & Signature", 8.5f, true, C_LABEL).setTextAlignment(TextAlignment.RIGHT))
            .add(p("Mayor / Registrar", 8f, false, C_GRAY).setTextAlignment(TextAlignment.RIGHT)));

        doc.add(s);
    }


    private void addTradeSignatureImages(Document doc, String firstSignature, String secondSignature, String middleTitle) {
        Table s = pct(new float[]{34f, 32f, 34f}).setMarginTop(8);
        s.addCell(tradeSigCell(firstSignature, "Seal & Signature", "Assistant to Registrar", TextAlignment.LEFT));
        s.addCell(new Cell()
            .setBorder(new SolidBorder(C_BORDER, 0.7f))
            .setPadding(6)
            .setMinHeight(58)
            .setVerticalAlignment(VerticalAlignment.MIDDLE)
            .add(p("[ OFFICIAL SEAL ]", 8f, false, C_GRAY).setTextAlignment(TextAlignment.CENTER))
            .add(p(middleTitle, 7.5f, false, C_GRAY).setTextAlignment(TextAlignment.CENTER)));
        s.addCell(tradeSigCell(secondSignature, "Seal & Signature", "Mayor / Registrar", TextAlignment.RIGHT));
        doc.add(s);
    }

    private Cell tradeSigCell(String signatureBase64, String label, String role, TextAlignment align) {
        Cell cell = new Cell()
                .setBorder(new SolidBorder(C_BORDER, 0.7f))
                .setPadding(6)
                .setMinHeight(58)
                .setTextAlignment(align);
        try {
            if (signatureBase64 != null && !signatureBase64.isBlank()) {
                Image sig = new Image(ImageDataFactory.create(decodeDataUrl(signatureBase64)))
                        .setWidth(95).setHeight(34);
                if (align == TextAlignment.RIGHT) sig.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);
                else sig.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.LEFT);
                cell.add(sig);
            } else {
                cell.add(p("\n____________________", 10f, false, C_TEXT).setTextAlignment(align));
            }
        } catch (Exception ex) {
            cell.add(p("\n____________________", 10f, false, C_TEXT).setTextAlignment(align));
        }
        cell.add(p(label, 8.5f, true, C_LABEL).setTextAlignment(align).setMarginTop(2));
        cell.add(p(role, 8f, false, C_GRAY).setTextAlignment(align));
        return cell;
    }

    private byte[] decodeDataUrl(String dataUrl) {
        String data = dataUrl == null ? "" : dataUrl.trim();
        int comma = data.indexOf(',');
        if (comma >= 0) data = data.substring(comma + 1);
        return java.util.Base64.getDecoder().decode(data);
    }

    private byte[] makeQR(String content, int size) throws Exception {
        BitMatrix m = new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size);
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < size; x++)
            for (int y = 0; y < size; y++)
                img.setRGB(x, y, m.get(x, y) ? 0x000000 : 0xFFFFFF);
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        ImageIO.write(img, "png", o);
        return o.toByteArray();
    }

    private String v(Object o) { 
    	return o == null ? "" : o.toString().trim(); }
    
    private String now() { 
    	return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
    
    private String exp(Integer y){ int yr = (y == null || y <= 0) ? 1 : y;
        return LocalDate.now().plusYears(yr).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
    
    private String expYr(int y) { 
    	return LocalDate.now().plusYears(Math.max(1, y)).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")); }
}
