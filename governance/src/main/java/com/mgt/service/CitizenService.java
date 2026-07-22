package com.mgt.service;

import java.time.LocalDateTime;
import java.io.IOException;

import java.util.List;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.mgt.dao.CitizenDAO;
import com.mgt.model.CitizenCertificate;

@Service
public class CitizenService {

    @Autowired
    CitizenDAO citizenDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public void create(CitizenCertificate citizen) {
        if (citizen.getApprovalStage() == null) citizen.setApprovalStage(0);
        if (citizen.getStatus() == null || citizen.getStatus().isBlank()) citizen.setStatus("Pending");
        citizenDAO.save(citizen);
        emailNotifier.sendApplicationReceived(
                citizen.getContact(),
                citizen.getName(),
                "নাগরিক সনদ",
                null
        );
    }

    public boolean existsByNid(String nid) {
        return citizenDAO.existsByNid(nid);
    }

    public List<CitizenCertificate> getall() {
        return citizenDAO.getall();
    }

    public List<CitizenCertificate> getByContact(String mobile) {
        return citizenDAO.getByContact(mobile);
    }

    public CitizenCertificate getById(int id) {
        return citizenDAO.getById(id);
    }

    public void updateStatus(int id, String status) {
        citizenDAO.updateStatus(id, status);

        // Approve / Reject notification
        CitizenCertificate citizen = citizenDAO.getById(id);
        if (citizen != null) {
            emailNotifier.sendStatusUpdate(
                    citizen.getContact(),
                    citizen.getName(),
                    "নাগরিক সনদ",
                    String.valueOf(citizen.getId()),
                    status,
                    null
            );
        }
    }

    public void delete(int id) {
        citizenDAO.delete(id);
    }

    public CitizenCertificate approveStep(int id, String officerUsername, String officerRole, String signatureBase64, String sealBase64) {
        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new IllegalArgumentException("Signature is required");
        }
        String resolvedSeal = resolveSeal(sealBase64);
        CitizenCertificate c = citizenDAO.getById(id);
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

        citizenDAO.saveOrUpdate(c);
        return c;
    }

    public CitizenCertificate updateSeal(int id, String sealBase64) {
        String resolvedSeal = resolveSeal(sealBase64);
        CitizenCertificate c = citizenDAO.getById(id);
        if (c == null) throw new IllegalArgumentException("Application not found");
        if (!"Approved".equalsIgnoreCase(c.getStatus())) {
            throw new IllegalStateException("Seal can be updated after final approval");
        }
        c.setFirstSeal(resolvedSeal);
        c.setSecondSeal(resolvedSeal);
        citizenDAO.saveOrUpdate(c);
        return c;
    }

    public CitizenCertificate ensureSealForDownload(int id) {
        CitizenCertificate c = citizenDAO.getById(id);
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
