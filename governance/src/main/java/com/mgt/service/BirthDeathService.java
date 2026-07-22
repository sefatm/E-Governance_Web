package com.mgt.service;

import java.time.LocalDateTime;
import java.io.IOException;

import java.util.List;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.mgt.dao.BirthDeathDAO;
import com.mgt.model.BirthDeathCertificate;

@Service
public class BirthDeathService {

    @Autowired
    BirthDeathDAO birthDeathDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public void create(BirthDeathCertificate birthDeath) {
        if (birthDeath.getApprovalStage() == null) birthDeath.setApprovalStage(0);
        if (birthDeath.getStatus() == null || birthDeath.getStatus().isBlank()) birthDeath.setStatus("Pending");
        BirthDeathCertificate saved = birthDeathDAO.save(birthDeath);

        String certNo = generateCertificateNo(saved.getType(), saved.getId());
        saved.setCertificateNo(certNo);
        birthDeathDAO.save(saved);

        // আবেদন জমার notification
        String contact = saved.getEmail() != null ? saved.getEmail() : saved.getContact();
        String serviceName = "Birth".equalsIgnoreCase(saved.getType()) ? "জন্ম নিবন্ধন সনদ" : "মৃত্যু নিবন্ধন সনদ";
        emailNotifier.sendApplicationReceived(
                contact,
                saved.getApplicantName() != null ? saved.getApplicantName() : "আবেদনকারী",
                serviceName,
                certNo
        );
    }

    private String generateCertificateNo(String type, int id) {
        String year   = String.valueOf(java.time.Year.now().getValue());
        String prefix = type.equalsIgnoreCase("Birth") ? "BIRTH" : "DEATH";
        return prefix + "-" + year + "-" + String.format("%05d", id);
    }

    public List<BirthDeathCertificate> getall() {
        return birthDeathDAO.getall();
    }

    public List<BirthDeathCertificate> findByMobile(String mobile) { return birthDeathDAO.findByMobile(mobile); }

    public void updateStatus(int id, String status) {
        birthDeathDAO.updateStatus(id, status);

        // Approve / Reject notification
        BirthDeathCertificate cert = birthDeathDAO.findById(id);
        if (cert != null) {
            String contact = cert.getEmail() != null ? cert.getEmail() : cert.getContact();
            String serviceName = "Birth".equalsIgnoreCase(cert.getType()) ? "জন্ম নিবন্ধন সনদ" : "মৃত্যু নিবন্ধন সনদ";
            emailNotifier.sendStatusUpdate(
                    contact,
                    cert.getApplicantName() != null ? cert.getApplicantName() : "আবেদনকারী",
                    serviceName,
                    cert.getCertificateNo(),
                    status,
                    null
            );
        }
    }

    public BirthDeathCertificate getById(int id) {
        return birthDeathDAO.findById(id);
    }

    public BirthDeathCertificate approveStep(int id, String officerUsername, String officerRole, String signatureBase64, String sealBase64) {
        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new IllegalArgumentException("Signature is required");
        }
        String resolvedSeal = resolveSeal(sealBase64);
        BirthDeathCertificate c = birthDeathDAO.findById(id);
        if (c == null) throw new IllegalArgumentException("Application not found");

        if (c.getApprovalStage() == null) c.setApprovalStage(0);
        String status = c.getStatus() == null ? "Pending" : c.getStatus().trim();
        if ("Pending".equalsIgnoreCase(status) || c.getApprovalStage() == 0) {
            if (!"ROLE_Department_Officer".equals(officerRole)) {
                throw new IllegalStateException("First approval can only be completed by a Department Officer");
            }
            c.setFirstApprovedBy(officerUsername);
            c.setFirstSignature(signatureBase64);
            c.setFirstSeal(resolvedSeal);
            c.setFirstApprovedAt(LocalDateTime.now());
            c.setApprovalStage(1);
            c.setStatus("First Approved");
        } else if ("First Approved".equalsIgnoreCase(status) || c.getApprovalStage() == 1) {
            boolean finalApprover = "ROLE_Admin_Municipal_Officer".equals(officerRole)
                    || "ROLE_Super_Admin".equals(officerRole);
            if (!finalApprover) {
                throw new IllegalStateException("Final approval can only be completed by an Admin or Super Admin");
            }
            if (officerUsername != null && officerUsername.equalsIgnoreCase(c.getFirstApprovedBy())) {
                throw new IllegalStateException("Second approval must be completed by a different officer");
            }
            c.setSecondApprovedBy(officerUsername);
            c.setSecondSignature(signatureBase64);
            c.setSecondSeal(resolvedSeal);
            c.setSecondApprovedAt(LocalDateTime.now());
            c.setApprovalStage(2);
            c.setStatus("Approved");
        } else {
            throw new IllegalStateException("Application is already finally approved or cannot be approved");
        }

        birthDeathDAO.save(c);
        return c;
    }

    public BirthDeathCertificate updateSeal(int id, String sealBase64) {
        String resolvedSeal = resolveSeal(sealBase64);
        BirthDeathCertificate c = birthDeathDAO.findById(id);
        if (c == null) throw new IllegalArgumentException("Application not found");
        if (!"Approved".equalsIgnoreCase(c.getStatus())) {
            throw new IllegalStateException("Seal can be updated after final approval");
        }
        c.setFirstSeal(resolvedSeal);
        c.setSecondSeal(resolvedSeal);
        birthDeathDAO.save(c);
        return c;
    }

    public BirthDeathCertificate ensureSealForDownload(int id) {
        BirthDeathCertificate c = birthDeathDAO.findById(id);
        if (c == null) return null;
        if ("Approved".equalsIgnoreCase(c.getStatus())) {
            if (c.getFirstSeal() == null || c.getFirstSeal().isBlank() ||
                c.getSecondSeal() == null || c.getSecondSeal().isBlank()) {
                throw new IllegalStateException("Uploaded seal is required for both approvals before PDF download");
            }
        }
        return c;
    }

    private String resolveSeal(String sealBase64) {
        if (sealBase64 != null && !sealBase64.isBlank()) return sealBase64;
        throw new IllegalArgumentException("Seal is required");
    }

}
