package com.mgt.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mgt.dao.TradeLicenseDAO;
import com.mgt.model.BusinessCategory;
import com.mgt.model.TradeLicenseApply;

@Service
public class TradeLicenseService {

    @Autowired private TradeLicenseDAO tradelicenseDAO;

    /**
     * ⚠️ KEY FIX: EmailService সরাসরি inject করা বাদ দেওয়া হয়েছে।
     * EmailService @Async method গুলো same bean থেকে private method হিসেবে
     * call করলে Spring proxy bypass হয় — @Async কাজ করে না।
     *
     * Solution: TradeLicenseEmailService আলাদা @Service এ রাখা হয়েছে।
     * এতে Spring proxy সঠিকভাবে @Async handle করতে পারে।
     */
    @Autowired private TradeLicenseEmailService tradeLicenseEmailService;

    private static final DateTimeFormatter DATE_FMT            = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final double            FINE_RATE_PER_MONTH = 0.05;
    private static final double            MAX_FINE_RATE       = 0.50;

    // ─── Create ──────────────────────────────────────────────────────────────
    public TradeLicenseApply create(TradeLicenseApply trade) {

        if (!BusinessCategory.isValid(trade.getBusinessType())) {
            throw new RuntimeException(
                "'" + trade.getBusinessType() + "' একটি অনুমোদিত Business Category নয়।");
        }

        if (tradelicenseDAO.existsByNidAndActiveStatus(trade.getNid())) {
            throw new RuntimeException(
                "এই NID দিয়ে ইতোমধ্যে একটি Pending বা Approved Trade License আবেদন রয়েছে।");
        }

        // Email sanitize
        if (trade.getEmail() != null) {
            trade.setEmail(trade.getEmail().replaceAll("[;,\\s]+$", "").trim());
            if (trade.getEmail().isBlank()) trade.setEmail(null);
        }

        trade.setLicenseNumber(generateLicenseNumber());
        trade.setStatus("Pending");
        TradeLicenseApply saved = tradelicenseDAO.save(trade);

        // ✅ FIX: আলাদা @Service এ call — @Async সঠিকভাবে কাজ করবে
        tradeLicenseEmailService.sendApplyReceived(saved);

        return saved;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────
    public List<TradeLicenseApply> getAll()  { return tradelicenseDAO.getall(); }
    public TradeLicenseApply getById(int id) { return tradelicenseDAO.getById(id); }

    public TradeLicenseApply ensureApprovedForDownload(TradeLicenseApply trade) {
        if (trade == null || !"Approved".equalsIgnoreCase(trade.getStatus())) return trade;
        boolean changed = false;
        if (trade.getApprovalStage() == null || trade.getApprovalStage() < 2) {
            trade.setApprovalStage(2);
            changed = true;
        }
        if (trade.getExpiryDate() == null) {
            int years = trade.getLicensePeriod() != null ? trade.getLicensePeriod() : 1;
            trade.setExpiryDate(LocalDate.now().plusYears(years));
            changed = true;
        }
        return changed ? tradelicenseDAO.update(trade) : trade;
    }

    // ─── Update Status ────────────────────────────────────────────────────────
    public void updateStatus(int id, String status) {
        tradelicenseDAO.updateStatus(id, status);

        TradeLicenseApply trade = tradelicenseDAO.getById(id);
        if (trade == null) return;

        if ("Approved".equalsIgnoreCase(status)) {
            int years = trade.getLicensePeriod() != null ? trade.getLicensePeriod() : 1;
            LocalDate expiry = LocalDate.now().plusYears(years);
            tradelicenseDAO.setExpiryDate(id, expiry);
            trade.setExpiryDate(expiry);
            // ✅ FIX: আলাদা bean এ call
            tradeLicenseEmailService.sendApproved(trade, expiry);

        } else if ("Rejected".equalsIgnoreCase(status)) {
            tradeLicenseEmailService.sendRejected(trade);
        }
    }

    public TradeLicenseApply approveStep(int id, String officerUsername, String officerRole, String signatureBase64) {
        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new IllegalArgumentException("Signature is required");
        }
        TradeLicenseApply trade = tradelicenseDAO.getById(id);
        if (trade == null) throw new IllegalArgumentException("Application not found");

        if (trade.getApprovalStage() == null) trade.setApprovalStage(0);
        String status = trade.getStatus() == null ? "Pending" : trade.getStatus().trim();

        if ("Pending".equalsIgnoreCase(status) || trade.getApprovalStage() == 0) {
            if (!"ROLE_Department_Officer".equals(officerRole)) {
                throw new IllegalStateException("First approval can only be completed by a Department Officer");
            }
            trade.setFirstApprovedBy(officerUsername);
            trade.setFirstSignature(signatureBase64);
            trade.setFirstApprovedAt(LocalDateTime.now());
            trade.setApprovalStage(1);
            trade.setStatus("First Approved");
        } else if ("First Approved".equalsIgnoreCase(status) || trade.getApprovalStage() == 1) {
            boolean finalApprover = "ROLE_Admin_Municipal_Officer".equals(officerRole)
                    || "ROLE_Super_Admin".equals(officerRole);
            if (!finalApprover) {
                throw new IllegalStateException("Final approval can only be completed by an Admin or Super Admin");
            }
            if (officerUsername != null && officerUsername.equalsIgnoreCase(trade.getFirstApprovedBy())) {
                throw new IllegalStateException("Second approval must be completed by a different officer");
            }
            trade.setSecondApprovedBy(officerUsername);
            trade.setSecondSignature(signatureBase64);
            trade.setSecondApprovedAt(LocalDateTime.now());
            trade.setApprovalStage(2);
            trade.setStatus("Approved");

            int years = trade.getLicensePeriod() != null ? trade.getLicensePeriod() : 1;
            LocalDate expiry = LocalDate.now().plusYears(years);
            trade.setExpiryDate(expiry);
            tradeLicenseEmailService.sendApproved(trade, expiry);
        } else {
            throw new IllegalStateException("Application is already finally approved or cannot be approved");
        }

        return tradelicenseDAO.update(trade);
    }

    // ─── Verify ───────────────────────────────────────────────────────────────
    public TradeLicenseApply verify(String licenseNumber, String dateOfBirth) {
        TradeLicenseApply trade = tradelicenseDAO.findByLicenseNumber(licenseNumber);
        if (trade == null) throw new RuntimeException("License পাওয়া যায়নি: " + licenseNumber);
        if (!dateOfBirth.equals(trade.getDateOfBirth())) throw new RuntimeException("জন্ম তারিখ মিলছে না।");
        return trade;
    }

    // ─── Late Fine ────────────────────────────────────────────────────────────
    public double calculateAndSaveLateFine(TradeLicenseApply trade) {
        if (trade.getExpiryDate() == null) return 0.0;
        LocalDate today = LocalDate.now();
        if (!today.isAfter(trade.getExpiryDate())) return 0.0;
        long monthsLate = java.time.temporal.ChronoUnit.MONTHS.between(trade.getExpiryDate(), today);
        if (monthsLate <= 0) return 0.0;
        double baseTax  = trade.getTax() != null ? trade.getTax() : 0.0;
        double fineRate = Math.min(monthsLate * FINE_RATE_PER_MONTH, MAX_FINE_RATE);
        double fine     = Math.round(baseTax * fineRate);
        if (fine > 0) tradelicenseDAO.updateLateFine(trade.getId(), fine, "Pending");
        return fine;
    }

    // ─── Schedulers ───────────────────────────────────────────────────────────
    @Scheduled(cron = "0 0 9 * * *")
    public void send60DayRenewalReminder() {
        for (TradeLicenseApply t : tradelicenseDAO.getExpiringIn60DaysNotReminded()) {
            tradeLicenseEmailService.sendRenewalReminder(t, 60);
            tradelicenseDAO.markReminder60Sent(t.getId());
        }
    }

    @Scheduled(cron = "0 5 9 * * *")
    public void send30DayRenewalReminder() {
        for (TradeLicenseApply t : tradelicenseDAO.getExpiringIn30DaysNotReminded()) {
            tradeLicenseEmailService.sendRenewalReminder(t, 30);
            tradelicenseDAO.markReminder30Sent(t.getId());
        }
    }

    private String generateLicenseNumber() {
        int year   = LocalDate.now().getYear();
        int random = (int)(Math.random() * 90000) + 10000;
        return "TL-" + year + "-" + random;
    }
}
