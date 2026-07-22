package com.mgt.service;

import com.mgt.dao.*;
import com.mgt.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class TcbDistributionService {

    @Autowired TcbStockDAO            stockDAO;
    @Autowired DistributionSessionDAO sessionDAO;
    @Autowired DistributionLogDAO     logDAO;

    @Autowired ApplicationEmailNotifier emailNotifier;
    @Autowired FamilyCardDAO  familyCardDAO;
    @Autowired FarmerCardDAO  farmerCardDAO;
    @Autowired LpgCardDAO     lpgCardDAO;
    @Autowired VgdCardDAO     vgdCardDAO;

    // ── SESSION ────────────────────────────────────────────────────────────────

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
        return m;
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
        String holderName, nid, ward, status, cardType;
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
