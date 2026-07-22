package com.mgt.service;

import java.time.LocalDateTime;
import java.io.IOException;

import java.util.List;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.mgt.dao.FamilyDAO;
import com.mgt.model.FamilyCertificate;

@Service
public class FamilyService {

    @Autowired private FamilyDAO fdao;
    @Autowired private ApplicationEmailNotifier emailNotifier;

    public void create(FamilyCertificate family) {
        if (family.getApprovalStage() == null) family.setApprovalStage(0);
        if (family.getStatus() == null || family.getStatus().isBlank()) family.setStatus("Pending");
        fdao.save(family);
        // আবেদন জমার confirmation email
        if (family.getEmail() != null && !family.getEmail().isBlank()) {
            emailNotifier.sendApplicationReceived(
                family.getEmail(),
                family.getHeadName(),
                "পারিবারিক সনদ আবেদন",
                "FAM-" + family.getId()
            );
        }
    }

    public List<FamilyCertificate> getAll()      { return fdao.getAll(); }
    public List<FamilyCertificate> findByContact(String mobile) { return fdao.findByContact(mobile); }
    public FamilyCertificate getById(int id)      { return fdao.getById(id); }

    public void update(int id, FamilyCertificate family) {
        family.setId(id);
        fdao.update(family);
    }

    public void updateStatus(int id, String status) {
        fdao.updateStatus(id, status);
        // Status update email
        FamilyCertificate fam = fdao.getById(id);
        if (fam != null && fam.getEmail() != null && !fam.getEmail().isBlank()) {
            emailNotifier.sendStatusUpdate(
                fam.getEmail(), fam.getHeadName(),
                "পারিবারিক সনদ আবেদন", "FAM-" + id,
                status, null
            );
        }
    }

    public void delete(int id) { fdao.delete(id); }

    public FamilyCertificate approveStep(int id, String officerUsername, String officerRole, String signatureBase64, String sealBase64) {
        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new IllegalArgumentException("Signature is required");
        }
        String resolvedSeal = resolveSeal(sealBase64);
        FamilyCertificate c = fdao.getById(id);
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

        fdao.update(c);
        return c;
    }

    public FamilyCertificate updateSeal(int id, String sealBase64) {
        String resolvedSeal = resolveSeal(sealBase64);
        FamilyCertificate c = fdao.getById(id);
        if (c == null) throw new IllegalArgumentException("Application not found");
        if (!"Approved".equalsIgnoreCase(c.getStatus())) {
            throw new IllegalStateException("Seal can be updated after final approval");
        }
        c.setFirstSeal(resolvedSeal);
        c.setSecondSeal(resolvedSeal);
        fdao.update(c);
        return c;
    }

    public FamilyCertificate ensureSealForDownload(int id) {
        FamilyCertificate c = fdao.getById(id);
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
