package com.mgt.service;

import com.mgt.dao.*;
import com.mgt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TcbDistributionService {

    @Autowired TcbStockDAO            stockDAO;
    @Autowired DistributionSessionDAO sessionDAO;
    @Autowired DistributionLogDAO     logDAO;

    @Autowired ApplicationEmailNotifier emailNotifier;
    @Autowired EmailService emailService;
    @Autowired SmsService smsService;
    @Autowired FamilyCardDAO  familyCardDAO;
    @Autowired FarmerCardDAO  farmerCardDAO;
    @Autowired LpgCardDAO     lpgCardDAO;
    @Autowired VgdCardDAO     vgdCardDAO;

    // ── SESSION ────────────────────────────────────────────────────────────────

    public Map<String, Object> openSession(String dealerName, String ward, String cycleMonth,
                                           String distributionDate, String distributionTime, String location) {
        List<DistributionSession> existing = sessionDAO.getOpenByWardAndCycle(ward, cycleMonth);
        if (!existing.isEmpty()) {
            DistributionSession s = existing.get(0);
            Map<String, Object> res = new LinkedHashMap<>();
            res.put("sessionId", s.getId());
            res.put("sessionCode", s.getSessionCode());
            res.put("message", "Existing session is being reused.");
            res.put("reused", true);
            TcbStock existingStock = stockDAO.getById(s.getStockId());
            res.put("stock", existingStock != null ? buildStockSummary(existingStock) : null);
            return res;
        }

        TcbStock stock = stockDAO.getByCycleAndWard(cycleMonth, ward);
        if (stock == null) {
            return Map.of("error", "No stock has been created for ward " + ward + " and cycle " + cycleMonth + ".");
        }

        int remaining = stock.getTotalCards() - stock.getDistributed();
        if (remaining <= 0) {
            return Map.of("error", "This ward stock is finished. Please create a new stock batch.");
        }

        DistributionSession session = new DistributionSession();
        session.setSessionCode("SES-" + ward + "-" + cycleMonth + "-"
                + String.format("%04d", (int)(Math.random() * 9000) + 1000));
        session.setStockId(stock.getId());
        session.setDealerName(dealerName);
        session.setWard(ward);
        session.setCycleMonth(cycleMonth);
        session.setStatus("OPEN");
        session.setDistributionDate(parseDate(distributionDate));
        session.setDistributionTime(blankToNull(distributionTime));
        session.setLocation(blankToNull(location));
        sessionDAO.save(session);

        int notified = notifyBeneficiaries(session, stock);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("sessionId", session.getId());
        res.put("sessionCode", session.getSessionCode());
        res.put("message", "Session started.");
        res.put("reused", false);
        res.put("notified", notified);
        res.put("stock", buildStockSummary(stock));
        return res;
    }

    public Map<String, Object> openSession(String dealerName, String ward, String cycleMonth) {
        List<DistributionSession> existing = sessionDAO.getOpenByWardAndCycle(ward, cycleMonth);
        if (!existing.isEmpty()) {
            DistributionSession s = existing.get(0);
            return Map.of(
                "sessionId",   s.getId(),
                "sessionCode", s.getSessionCode(),
                "message",     "বিদ্যমান session পুনরায় ব্যবহার করা হচ্ছে।",
                "reused",      true
            );
        }

        TcbStock stock = stockDAO.getByCycleAndWard(cycleMonth, ward);
        if (stock == null) {
            return Map.of("error",
                "এই ward (" + ward + ") ও cycle (" + cycleMonth + ")-এর জন্য কোনো stock তৈরি হয়নি।");
        }

        int remaining = stock.getTotalCards() - stock.getDistributed();
        if (remaining <= 0) {
            return Map.of("error", "এই ward-এর stock শেষ হয়ে গেছে। নতুন stock তৈরি করুন।");
        }

        DistributionSession session = new DistributionSession();
        session.setSessionCode("SES-" + ward + "-" + cycleMonth + "-"
                + String.format("%04d", (int)(Math.random() * 9000) + 1000));
        session.setStockId(stock.getId());
        session.setDealerName(dealerName);
        session.setWard(ward);
        session.setCycleMonth(cycleMonth);
        session.setStatus("OPEN");
        sessionDAO.save(session);

        return Map.of(
            "sessionId",   session.getId(),
            "sessionCode", session.getSessionCode(),
            "message",     "Session শুরু হয়েছে।",
            "reused",      false,
            "stock",       buildStockSummary(stock)
        );
    }

    public Map<String, Object> closeSession(int sessionId) {
        sessionDAO.close(sessionId);
        return Map.of("message", "Session বন্ধ হয়েছে।");
    }

    public Map<String, Object> getSessionStatus(int sessionId) {
        DistributionSession s = sessionDAO.getById(sessionId);
        if (s == null) return Map.of("error", "Session পাওয়া যায়নি।");

        List<DistributionLog> logs = logDAO.getBySession(sessionId);
        TcbStock stock = stockDAO.getById(s.getStockId());

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("session", s);
        res.put("logs",    logs);
        res.put("stock",   stock != null ? buildStockSummary(stock) : null);
        return res;
    }

    public List<DistributionSession> getAllSessions() {
        return sessionDAO.getAll();
    }

    // ── SCAN ───────────────────────────────────────────────────────────────────

    public Map<String, Object> scan(int sessionId, String rawCardNo, String scannedBy) {

        String cardNo = parseCardNo(rawCardNo);

        DistributionSession session = sessionDAO.getById(sessionId);
        if (session == null)
            return fail("Session পাওয়া যায়নি।");
        if ("CLOSED".equals(session.getStatus()))
            return fail("এই session ইতোমধ্যে বন্ধ হয়ে গেছে।");

        // Duplicate check within this cycle
        if (logDAO.alreadyReceivedInCycle(cardNo, session.getCycleMonth()))
            return fail("এই কার্ডে এই মাসে ইতোমধ্যে মাল দেওয়া হয়েছে।");

        // Card lookup across all 4 types
        CardInfo info = lookupCard(cardNo);
        if (info == null)
            return fail("কার্ড নম্বর '" + cardNo + "' পাওয়া যায়নি।");
        if (!"Approved".equalsIgnoreCase(info.status))
            return fail("কার্ডটি অনুমোদিত নয়। বর্তমান অবস্থা: " + info.status);
        if (!session.getWard().equals(info.ward))
            return fail("কার্ডের ward (" + info.ward + ") এই session-এর ward ("
                    + session.getWard() + ") এর সাথে মেলে না।");

        Entitlement ent = getEntitlement(info.cardType);

        TcbStock stock = stockDAO.getById(session.getStockId());
        if (stock == null)
            return fail("Stock তথ্য পাওয়া যায়নি।");
        if (ent.oil.compareTo(BigDecimal.ZERO)    > 0 &&
                stock.getOilLitre().compareTo(ent.oil)    < 0)
            return fail("পর্যাপ্ত তেলের stock নেই।");
        if (ent.rice.compareTo(BigDecimal.ZERO)   > 0 &&
                stock.getRiceKg().compareTo(ent.rice)     < 0)
            return fail("পর্যাপ্ত চালের stock নেই।");
        if (ent.lentil.compareTo(BigDecimal.ZERO) > 0 &&
                stock.getLentilKg().compareTo(ent.lentil) < 0)
            return fail("পর্যাপ্ত ডালের stock নেই।");

        int remaining = stock.getTotalCards() - stock.getDistributed();
        if (remaining <= 0)
            return fail("এই stock batch-এর সকল কার্ডের বিতরণ সম্পন্ন হয়েছে।");

        // Record
        DistributionLog log = new DistributionLog();
        log.setSessionId(sessionId);
        log.setCardNo(cardNo);
        log.setCardType(info.cardType);
        log.setHolderName(info.holderName);
        log.setNid(info.nid);
        log.setWard(info.ward);
        log.setOilLitre(ent.oil);
        log.setRiceKg(ent.rice);
        log.setLentilKg(ent.lentil);
        log.setSugarKg(ent.sugar);
        log.setCashAmount(ent.cash);
        log.setScannedBy(scannedBy);
        logDAO.save(log);

        stockDAO.deductStock(stock.getId(), ent.oil, ent.rice, ent.lentil, ent.sugar, ent.cash);
        sessionDAO.incrementScanned(sessionId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success",    true);
        res.put("holderName", info.holderName);
        res.put("cardNo",     cardNo);
        res.put("cardType",   info.cardType);
        res.put("ward",       info.ward);
        res.put("oil",        ent.oil);
        res.put("rice",       ent.rice);
        res.put("lentil",     ent.lentil);
        res.put("sugar",      ent.sugar);
        res.put("cash",       ent.cash);
        res.put("scannedAt",  LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        res.put("message",    info.holderName + "-কে মাল দেওয়া হয়েছে ✓");

        // ✅ Distribution confirmation email to citizen (contact lookup by NID)
        String contactEmail = getContactByNid(info.nid, info.cardType);
        if (contactEmail != null) {
            // Build itemized list with quantity + price (reuse stock already loaded above)
            StringBuilder items = new StringBuilder();
            if (ent.oil.compareTo(BigDecimal.ZERO) > 0) {
                items.append("তেল: ").append(ent.oil).append(" লিটার");
                if (stock != null && stock.getOilPricePerLitre().compareTo(BigDecimal.ZERO) > 0)
                    items.append(" × ৳").append(stock.getOilPricePerLitre()).append("/L");
            }
            if (ent.rice.compareTo(BigDecimal.ZERO) > 0) {
                if (items.length() > 0) items.append(", ");
                items.append("চাল: ").append(ent.rice).append(" কেজি");
                if (stock != null && stock.getRicePricePerKg().compareTo(BigDecimal.ZERO) > 0)
                    items.append(" × ৳").append(stock.getRicePricePerKg()).append("/kg");
            }
            if (ent.lentil.compareTo(BigDecimal.ZERO) > 0) {
                if (items.length() > 0) items.append(", ");
                items.append("ডাল: ").append(ent.lentil).append(" কেজি");
                if (stock != null && stock.getLentilPricePerKg().compareTo(BigDecimal.ZERO) > 0)
                    items.append(" × ৳").append(stock.getLentilPricePerKg()).append("/kg");
            }
            if (ent.sugar.compareTo(BigDecimal.ZERO) > 0) {
                if (items.length() > 0) items.append(", ");
                items.append("চিনি: ").append(ent.sugar).append(" কেজি");
                if (stock != null && stock.getSugarPricePerKg().compareTo(BigDecimal.ZERO) > 0)
                    items.append(" × ৳").append(stock.getSugarPricePerKg()).append("/kg");
            }
            if (ent.cash.compareTo(BigDecimal.ZERO) > 0) {
                if (items.length() > 0) items.append(", ");
                items.append("নগদ: ৳").append(ent.cash);
            }
            emailNotifier.sendDistributionConfirmation(
                    contactEmail,
                    info.holderName,
                    cardNo,
                    "TCB কার্ড (" + info.cardType + ")",
                    session.getCycleMonth(),
                    items.length() > 0 ? items.toString() : "TCB পণ্য বিতরণ",
                    scannedBy
            );
        }
        return res;
    }

    // ── STOCK ──────────────────────────────────────────────────────────────────

    public TcbStock createStock(TcbStock stock) {
        return stockDAO.save(stock);
    }

    public List<TcbStock> getAllStock() {
        return stockDAO.getAll();
    }

    // ── HISTORY ────────────────────────────────────────────────────────────────

    public List<DistributionLog> getHistoryByCard(String rawCardNo) {
        String cardNo = parseCardNo(rawCardNo);
        return logDAO.getByCardNo(cardNo);
    }

    // ── HELPERS ────────────────────────────────────────────────────────────────
    private String parseCardNo(String raw) {
        if (raw == null) return "";
        String value = raw.trim();
        if (value.isEmpty()) return "";

        // New standard payload: EGOV_CARD|TYPE=LPG|CARD_NO=LPG-2026-0001
        if (value.startsWith("EGOV_CARD|")) {
            for (String part : value.split("\\|")) {
                int eq = part.indexOf('=');
                if (eq > 0 && "CARD_NO".equalsIgnoreCase(part.substring(0, eq).trim())) {
                    return part.substring(eq + 1).trim();
                }
            }
            return "";
        }

        // Legacy payloads kept for already-issued cards.
        String[] prefixes = { "FARMER_CARD:", "LPG_CARD:", "VGD_CARD:", "CARD:" };
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) {
                value = value.substring(prefix.length());
                int pipeIdx = value.indexOf('|');
                if (pipeIdx >= 0) value = value.substring(0, pipeIdx);
                return value.trim();
            }
        }

        // Plain card number/manual entry.
        return value;
    }

    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "message", msg);
    }

    private Map<String, Object> buildStockSummary(TcbStock s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          s.getId());
        m.put("batchLabel",  s.getBatchLabel());
        m.put("ward",        s.getWard());
        m.put("cycleMonth",  s.getCycleMonth());
        m.put("oilLitre",    s.getOilLitre());
        m.put("riceKg",      s.getRiceKg());
        m.put("lentilKg",    s.getLentilKg());
        m.put("sugarKg",     s.getSugarKg());
        m.put("cashAmount",  s.getCashAmount());
        m.put("totalCards",  s.getTotalCards());
        m.put("distributed", s.getDistributed());
        m.put("remaining",   s.getTotalCards() - s.getDistributed());
        m.put("oilPricePerLitre", s.getOilPricePerLitre());
        m.put("ricePricePerKg", s.getRicePricePerKg());
        m.put("lentilPricePerKg", s.getLentilPricePerKg());
        m.put("sugarPricePerKg", s.getSugarPricePerKg());
        return m;
    }

    private int notifyBeneficiaries(DistributionSession session, TcbStock stock) {
        int sent = 0;
        for (CardInfo info : getApprovedCardsForWard(session.getWard())) {
            Entitlement ent = getEntitlement(info.cardType);
            BigDecimal payable = calculatePayable(ent, stock);
            String items = buildEntitlementItems(ent, stock);
            String noticeText = buildSessionNoticeText(session, info, items, payable);

            if (isMobile(info.contact)) {
                smsService.send(info.contact, noticeText);
                sent++;
            }
            if (isEmail(info.contact)) {
                emailService.sendHtml(info.contact, "TCB distribution notice - " + session.getCycleMonth(),
                        buildSessionNoticeHtml(info, session, items, payable));
                sent++;
            }
        }
        return sent;
    }

    private List<CardInfo> getApprovedCardsForWard(String ward) {
        List<CardInfo> cards = new ArrayList<>();
        cards.addAll(queryBeneficiaries("family_card", "holder_name", "'FAMILY'", ward));
        cards.addAll(queryBeneficiaries("farmer_card", "farmer_name", "'FARMER'", ward));
        cards.addAll(queryBeneficiaries("lpg_card", "holder_name", "'LPG'", ward));
        cards.addAll(queryBeneficiaries("vgd_card", "holder_name", "card_type", ward));
        return cards;
    }

    @SuppressWarnings("unchecked")
    private List<CardInfo> queryBeneficiaries(String table, String nameColumn, String typeExpr, String ward) {
        String sql = "SELECT " + nameColumn + ", nid, ward, status, " + typeExpr + ", card_no, contact "
                + "FROM " + table + " WHERE ward = :w AND UPPER(status) = 'APPROVED'";
        List<Object[]> rows = familyCardDAO.getEm()
                .createNativeQuery(sql)
                .setParameter("w", ward)
                .getResultList();

        List<CardInfo> result = new ArrayList<>();
        for (Object[] row : rows) {
            CardInfo info = new CardInfo(str(row[0]), str(row[1]), str(row[2]), str(row[3]), str(row[4]));
            info.cardNo = str(row[5]);
            info.contact = str(row[6]);
            result.add(info);
        }
        return result;
    }

    private BigDecimal calculatePayable(Entitlement ent, TcbStock stock) {
        return ent.oil.multiply(stock.getOilPricePerLitre())
                .add(ent.rice.multiply(stock.getRicePricePerKg()))
                .add(ent.lentil.multiply(stock.getLentilPricePerKg()))
                .add(ent.sugar.multiply(stock.getSugarPricePerKg()));
    }

    private String buildEntitlementItems(Entitlement ent, TcbStock stock) {
        List<String> items = new ArrayList<>();
        if (ent.oil.compareTo(BigDecimal.ZERO) > 0)
            items.add("Oil " + qty(ent.oil) + "L @ Tk " + money(stock.getOilPricePerLitre()) + "/L");
        if (ent.rice.compareTo(BigDecimal.ZERO) > 0)
            items.add("Rice " + qty(ent.rice) + "kg @ Tk " + money(stock.getRicePricePerKg()) + "/kg");
        if (ent.lentil.compareTo(BigDecimal.ZERO) > 0)
            items.add("Lentil " + qty(ent.lentil) + "kg @ Tk " + money(stock.getLentilPricePerKg()) + "/kg");
        if (ent.sugar.compareTo(BigDecimal.ZERO) > 0)
            items.add("Sugar " + qty(ent.sugar) + "kg @ Tk " + money(stock.getSugarPricePerKg()) + "/kg");
        return String.join(", ", items);
    }

    private String buildSessionNoticeText(DistributionSession s, CardInfo info, String items, BigDecimal payable) {
        return "TCB notice: " + info.holderName + ", card " + info.cardNo
                + ". Bring Tk " + money(payable)
                + ". Items: " + items
                + ". Date: " + formatDate(s.getDistributionDate())
                + ", Time: " + valueOrDash(s.getDistributionTime())
                + ", Location: " + valueOrDash(s.getLocation())
                + ". Ward " + s.getWard() + ", " + s.getCycleMonth() + ".";
    }

    private String buildSessionNoticeHtml(CardInfo info, DistributionSession s, String items, BigDecimal payable) {
        return "<div style='font-family:Segoe UI,Arial,sans-serif;background:#f1f5f9;padding:20px'>"
                + "<div style='max-width:620px;margin:auto;background:#fff;border-radius:12px;overflow:hidden'>"
                + "<div style='background:#064e3b;color:#fff;padding:24px;border-bottom:4px solid #f59e0b'>"
                + "<h2 style='margin:0'>TCB Distribution Notice</h2>"
                + "<p style='margin:6px 0 0;color:#d1fae5'>Municipal E-Governance Portal</p></div>"
                + "<div style='padding:24px;color:#1f2937'>"
                + "<p>Dear <b>" + info.holderName + "</b>,</p>"
                + "<p>Your TCB goods will be distributed as scheduled below. Please bring the exact cash amount.</p>"
                + "<table style='width:100%;border-collapse:collapse;background:#f8fafc;border:1px solid #dbe5df'>"
                + row("Card No", info.cardNo)
                + row("Items", items)
                + row("Cash to bring", "Tk " + money(payable))
                + row("Date", formatDate(s.getDistributionDate()))
                + row("Time", valueOrDash(s.getDistributionTime()))
                + row("Location", valueOrDash(s.getLocation()))
                + row("Ward / Month", s.getWard() + " / " + s.getCycleMonth())
                + "</table></div></div></div>";
    }

    private String row(String label, String value) {
        return "<tr><td style='padding:10px;border-bottom:1px solid #e5e7eb;color:#065f46;font-weight:700;width:150px'>"
                + label + "</td><td style='padding:10px;border-bottom:1px solid #e5e7eb'>" + value + "</td></tr>";
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        try { return LocalDate.parse(value); }
        catch (Exception e) { return null; }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String money(BigDecimal value) {
        return qty(value != null ? value : BigDecimal.ZERO);
    }

    private String qty(BigDecimal value) {
        return (value != null ? value : BigDecimal.ZERO).stripTrailingZeros().toPlainString();
    }

    private String str(Object value) {
        return value == null ? "" : value.toString();
    }

    private boolean isEmail(String value) {
        return value != null && value.contains("@") && value.contains(".");
    }

    private boolean isMobile(String value) {
        if (value == null) return false;
        return value.replaceAll("[^0-9]", "").matches("(88)?01[0-9]{9}");
    }

    /** NID দিয়ে citizen-এর contact (email) বের করে — যেকোনো কার্ড টেবিল থেকে */
    private String getContactByNid(String nid, String cardType) {
        if (nid == null || nid.isBlank()) return null;
        try {
            String table = switch (cardType) {
                case "FARMER" -> "farmer_card";
                case "LPG"    -> "lpg_card";
                case "VGD", "VGF" -> "vgd_card";
                default       -> "family_card";
            };
            String nameCol = "FARMER".equals(cardType) ? "farmer_name" : "holder_name";
            List<?> rows = familyCardDAO.getEm()
                    .createNativeQuery("SELECT contact FROM " + table + " WHERE nid = :n LIMIT 1")
                    .setParameter("n", nid)
                    .getResultList();
            return rows.isEmpty() ? null : (String) rows.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private CardInfo lookupCard(String cardNo) {
        Object[] row;

        row = queryCard("family_card", cardNo);
        if (row != null) return toInfo(row, "FAMILY");

        row = queryCard("farmer_card", cardNo);
        if (row != null) return toInfo(row, "FARMER");

        row = queryCard("lpg_card", cardNo);
        if (row != null) return toInfo(row, "LPG");

        // vgd_card has its own card_type column (VGD or VGF)
        row = queryCard("vgd_card", cardNo);
        if (row != null) {
            String cardType = row[4] != null ? (String) row[4] : "VGD";
            return new CardInfo(
                (String) row[0], (String) row[1],
                (String) row[2], (String) row[3],
                cardType
            );
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private Object[] queryCard(String table, String cardNo) {
        boolean isVgd = table.equals("vgd_card");
        String sql = "SELECT holder_name, nid, ward, status, "
                + (isVgd ? "card_type" : "NULL")
                + " FROM " + table + " WHERE card_no = :cn LIMIT 1";

        List<Object[]> rows = familyCardDAO.getEm()
                .createNativeQuery(sql)
                .setParameter("cn", cardNo)
                .getResultList();

        return rows.isEmpty() ? null : rows.get(0);
    }

    private CardInfo toInfo(Object[] row, String cardType) {
        return new CardInfo(
            row[0] != null ? (String) row[0] : "",
            row[1] != null ? (String) row[1] : "",
            row[2] != null ? (String) row[2] : "",
            row[3] != null ? (String) row[3] : "",
            cardType
        );
    }

    private Entitlement getEntitlement(String cardType) {
        return switch (cardType.toUpperCase()) {
            case "VGD"    -> new Entitlement(BigDecimal.ZERO, bd(25), BigDecimal.ZERO, BigDecimal.ZERO, bd(500));
            case "VGF"    -> new Entitlement(BigDecimal.ZERO, bd(20), BigDecimal.ZERO, BigDecimal.ZERO, bd(500));
            case "LPG"    -> new Entitlement(bd(2),           bd(3),  bd(1),           bd("0.5"),       BigDecimal.ZERO);
            case "FARMER" -> new Entitlement(bd(1),           bd(10), bd(2),           bd(1),           BigDecimal.ZERO);
            default       -> new Entitlement(bd(2),           bd(5),  bd(2),           bd(1),           BigDecimal.ZERO); 
        };
    }

    private BigDecimal bd(double v) { return BigDecimal.valueOf(v); }
    private BigDecimal bd(String v) { return new BigDecimal(v); }

    // ── Inner helpers ──────────────────────────────────────────────────────────

    private static class CardInfo {
        String holderName, nid, ward, status, cardType, cardNo, contact;
        CardInfo(String h, String n, String w, String s, String t) {
            holderName = h; nid = n; ward = w; status = s; cardType = t;
        }
    }

    private static class Entitlement {
        BigDecimal oil, rice, lentil, sugar, cash;
        Entitlement(BigDecimal o, BigDecimal r, BigDecimal l, BigDecimal s, BigDecimal c) {
            oil = o; rice = r; lentil = l; sugar = s; cash = c;
        }
    }
}
