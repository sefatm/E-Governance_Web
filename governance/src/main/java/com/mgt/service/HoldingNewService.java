package com.mgt.service;

import java.util.List;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.HoldingNewDAO;
import com.mgt.model.HoldingNewRegistration;

@Service
public class HoldingNewService {

    @Autowired
    HoldingNewDAO hnDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public HoldingNewRegistration create(HoldingNewRegistration holding) {
        hnDAO.save(holding);

        // আবেদন জমার notification
        String contact = holding.getEmail() != null ? holding.getEmail() : holding.getMobile();
        emailNotifier.sendApplicationReceived(
                contact,
                holding.getApplicantName(),
                "নতুন হোল্ডিং নিবন্ধন",
                holding.getHoldingNo()
        );
        return holding;
    }

    public List<HoldingNewRegistration> getall() {
        return hnDAO.getall();
    }

    public HoldingNewRegistration getById(int id) {
        return hnDAO.getById(id);
    }

    public void updateStatus(int id, String status) {
        hnDAO.updateStatus(id, status);

        // Approve / Reject notification
        HoldingNewRegistration h = hnDAO.getById(id);
        if (h != null) {
            String contact = h.getEmail() != null ? h.getEmail() : h.getMobile();
            emailNotifier.sendStatusUpdate(
                    contact,
                    h.getApplicantName(),
                    "নতুন হোল্ডিং নিবন্ধন",
                    h.getHoldingNo(),
                    status,
                    null
            );
        }
    }

    public HoldingNewRegistration approveStep(int id, String officerUsername, String officerRole, String signatureBase64, String sealBase64) {
        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new IllegalArgumentException("Signature is required");
        }
        String resolvedSeal = requireSeal(sealBase64);
        HoldingNewRegistration h = hnDAO.getById(id);
        if (h == null) throw new IllegalArgumentException("Application not found");

        if (h.getApprovalStage() == null) h.setApprovalStage(0);
        String status = h.getStatus() == null ? "Pending" : h.getStatus().trim();

        if ("Pending".equalsIgnoreCase(status) || h.getApprovalStage() == 0) {
            if (!"ROLE_Department_Officer".equals(officerRole)) {
                throw new IllegalStateException("First approval can only be completed by a Department Officer");
            }
            h.setFirstApprovedBy(officerUsername);
            h.setFirstSignature(signatureBase64);
            h.setFirstSeal(resolvedSeal);
            h.setFirstApprovedAt(LocalDateTime.now());
            h.setApprovalStage(1);
            h.setStatus("First Approved");
        } else if ("First Approved".equalsIgnoreCase(status) || h.getApprovalStage() == 1) {
            boolean finalApprover = "ROLE_Admin_Municipal_Officer".equals(officerRole)
                    || "ROLE_Super_Admin".equals(officerRole);
            if (!finalApprover) {
                throw new IllegalStateException("Final approval can only be completed by an Admin or Super Admin");
            }
            if (officerUsername != null && officerUsername.equalsIgnoreCase(h.getFirstApprovedBy())) {
                throw new IllegalStateException("Second approval must be completed by a different officer");
            }
            h.setSecondApprovedBy(officerUsername);
            h.setSecondSignature(signatureBase64);
            h.setSecondSeal(resolvedSeal);
            h.setSecondApprovedAt(LocalDateTime.now());
            h.setApprovalStage(2);
            h.setStatus("Approved");
        } else {
            throw new IllegalStateException("Application is already finally approved or cannot be approved");
        }

        hnDAO.update(h);
        return h;
    }

    private String requireSeal(String sealBase64) {
        if (sealBase64 != null && !sealBase64.isBlank()) return sealBase64;
        throw new IllegalArgumentException("Seal is required");
    }

    public void update(HoldingNewRegistration h) {
        hnDAO.update(h);
    }

    public void updateLocation(int id, double lat, double lng) {
        hnDAO.updateLocation(id, lat, lng);
    }
}
