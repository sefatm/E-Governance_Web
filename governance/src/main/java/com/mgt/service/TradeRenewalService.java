package com.mgt.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mgt.dao.TradeLicenseDAO;
import com.mgt.dao.TradeRenewalDAO;
import com.mgt.model.TradeLicenseApply;
import com.mgt.model.TradeRenewal;

import java.util.List;

@Service
public class TradeRenewalService {

    @Autowired private TradeRenewalDAO     tradeRenewalDAO;
    @Autowired private TradeLicenseDAO     tradeLicenseDAO;
    @Autowired private TradeLicenseService tradeLicenseService;

    /**
     * ✅ FIX: email method গুলো এই same class এ @Async দিয়ে রাখা যাবে না।
     * TradeRenewalEmailService আলাদা bean এ রাখা হয়েছে।
     */
    @Autowired private TradeRenewalEmailService renewalEmailService;

    // ─── Create ───────────────────────────────────────────────────────────────
    public TradeRenewal create(TradeRenewal renewal) {
        renewal.setStatus("Pending");

        // Email sanitize
        if (renewal.getEmail() != null) {
            renewal.setEmail(renewal.getEmail().replaceAll("[;,\\s]+$", "").trim());
            if (renewal.getEmail().isBlank()) renewal.setEmail(null);
        }

        // Late fine check
        if (renewal.getOriginalLicense() != null) {
            TradeLicenseApply license = tradeLicenseDAO.getById(
                renewal.getOriginalLicense().getId()
            );
            if (license != null) {
                double fine = tradeLicenseService.calculateAndSaveLateFine(license);
                if (fine > 0) {
                    renewal.setLateFineAmount(fine);
                    renewal.setLateFineStatus("Pending");
                }
            }
        }

        TradeRenewal saved = tradeRenewalDAO.save(renewal);

        // ✅ FIX: আলাদা bean এ @Async call
        renewalEmailService.sendApplyReceived(saved);

        return saved;
    }

    // ─── Read ─────────────────────────────────────────────────────────────────
    public List<TradeRenewal> getAll()   { return tradeRenewalDAO.getall(); }
    public TradeRenewal getById(int id)  { return tradeRenewalDAO.getById(id); }

    public TradeRenewal ensureApprovedForDownload(TradeRenewal renewal) {
        if (renewal == null || !"Approved".equalsIgnoreCase(renewal.getStatus())) return renewal;
        if (renewal.getApprovalStage() == null || renewal.getApprovalStage() < 2) {
            renewal.setApprovalStage(2);
            return tradeRenewalDAO.update(renewal);
        }
        return renewal;
    }

    // ─── Update Status ────────────────────────────────────────────────────────
    public void updateStatus(int id, String status) {
        tradeRenewalDAO.updateStatus(id, status);

        TradeRenewal renewal = tradeRenewalDAO.getById(id);
        if (renewal == null) return;

        if ("Approved".equalsIgnoreCase(status)) {
            if (renewal.getOriginalLicense() != null) {
                TradeLicenseApply license = tradeLicenseDAO.getById(
                    renewal.getOriginalLicense().getId()
                );
                if (license != null) {
                    LocalDate baseDate = license.getExpiryDate() != null
                        ? license.getExpiryDate() : LocalDate.now();
                    if (baseDate.isBefore(LocalDate.now())) baseDate = LocalDate.now();
                    LocalDate newExpiry = baseDate.plusYears(renewal.getRenewalPeriod());

                    tradeLicenseDAO.setExpiryDate(license.getId(), newExpiry);
                    license.setReminder30Sent(false);
                    license.setReminder60Sent(false);
                    license.setLateFineStatus("Paid");
                    tradeLicenseDAO.update(license);

                    // ✅ FIX: আলাদা bean এ call
                    renewalEmailService.sendApproved(renewal, newExpiry);
                }
            }
        } else if ("Rejected".equalsIgnoreCase(status)) {
            renewalEmailService.sendRejected(renewal);
        }
    }


    // ─── Two-step renewal approval: signature only ───────────────────────────
    public TradeRenewal approveStep(int id, String officerUsername, String officerRole, String signatureBase64) {
        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new IllegalArgumentException("Signature is required");
        }

        TradeRenewal renewal = tradeRenewalDAO.getById(id);
        if (renewal == null) throw new IllegalArgumentException("Renewal application not found");
        if (renewal.getApprovalStage() == null) renewal.setApprovalStage(0);

        String status = renewal.getStatus() == null ? "Pending" : renewal.getStatus().trim();

        if ("Pending".equalsIgnoreCase(status) || renewal.getApprovalStage() == 0) {
            if (!"ROLE_Department_Officer".equals(officerRole)) {
                throw new IllegalStateException("First approval can only be completed by a Department Officer");
            }
            renewal.setFirstApprovedBy(officerUsername);
            renewal.setFirstSignature(signatureBase64);
            renewal.setFirstApprovedAt(LocalDateTime.now());
            renewal.setApprovalStage(1);
            renewal.setStatus("First Approved");
            return tradeRenewalDAO.update(renewal);
        }

        if ("First Approved".equalsIgnoreCase(status) || renewal.getApprovalStage() == 1) {
            boolean finalApprover = "ROLE_Admin_Municipal_Officer".equals(officerRole)
                    || "ROLE_Super_Admin".equals(officerRole);
            if (!finalApprover) {
                throw new IllegalStateException("Final approval can only be completed by an Admin or Super Admin");
            }
            if (officerUsername != null && officerUsername.equalsIgnoreCase(renewal.getFirstApprovedBy())) {
                throw new IllegalStateException("Second approval must be completed by a different officer");
            }
            renewal.setSecondApprovedBy(officerUsername);
            renewal.setSecondSignature(signatureBase64);
            renewal.setSecondApprovedAt(LocalDateTime.now());
            renewal.setApprovalStage(2);
            renewal.setStatus("Approved");

            if (renewal.getOriginalLicense() != null) {
                TradeLicenseApply license = tradeLicenseDAO.getById(renewal.getOriginalLicense().getId());
                if (license != null) {
                    LocalDate baseDate = license.getExpiryDate() != null ? license.getExpiryDate() : LocalDate.now();
                    if (baseDate.isBefore(LocalDate.now())) baseDate = LocalDate.now();
                    LocalDate newExpiry = baseDate.plusYears(renewal.getRenewalPeriod());
                    tradeLicenseDAO.setExpiryDate(license.getId(), newExpiry);
                    license.setReminder30Sent(false);
                    license.setReminder60Sent(false);
                    license.setLateFineStatus("Paid");
                    tradeLicenseDAO.update(license);
                    renewalEmailService.sendApproved(renewal, newExpiry);
                }
            }
            return tradeRenewalDAO.update(renewal);
        }

        throw new IllegalStateException("Renewal is already finally approved or cannot be approved");
    }

    // ─── Verify ───────────────────────────────────────────────────────────────
    public TradeRenewal verifyByLicenseNumber(String licenseNumber) {
        TradeRenewal renewal = tradeRenewalDAO.findByLicenseNumber(licenseNumber);
        if (renewal == null)
            throw new RuntimeException("Renewal পাওয়া যায়নি: " + licenseNumber);
        return renewal;
    }
}
