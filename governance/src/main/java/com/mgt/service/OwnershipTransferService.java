package com.mgt.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.dao.OwnershipTransferDAO;
import com.mgt.model.OwnershipTransfer;

@Service
public class OwnershipTransferService {

    @Autowired
    OwnershipTransferDAO ownerDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    public void create(
            OwnershipTransfer owner,
            MultipartFile currentOwnerNidFile,
            MultipartFile newOwnerNidFile,
            MultipartFile deedFile
    ) throws IOException {
        owner.setCurrentOwnerNidFileUrl(saveFile(currentOwnerNidFile, "ot_curr_nid_"));
        owner.setNewOwnerNidFileUrl(    saveFile(newOwnerNidFile,     "ot_new_nid_"));
        owner.setDeedFileUrl(           saveFile(deedFile,            "ot_deed_"));
        ownerDAO.save(owner);

        // আবেদন জমার notification
        emailNotifier.sendApplicationReceived(
                owner.getContact(),
                owner.getCurrentOwner(),
                "মালিকানা হস্তান্তর",
                String.valueOf(owner.getId())
        );
    }

    public List<OwnershipTransfer> getall() {
        return ownerDAO.getall();
    }

    public OwnershipTransfer getById(int id) {
        return ownerDAO.getById(id);
    }

    public void updateStatus(int id, String status, String rejectReason) {
        ownerDAO.updateStatus(id, status, rejectReason);

        // Approve / Reject notification
        OwnershipTransfer owner = ownerDAO.getById(id);
        if (owner != null) {
            emailNotifier.sendStatusUpdate(
                    owner.getContact(),
                    owner.getCurrentOwner(),
                    "মালিকানা হস্তান্তর",
                    String.valueOf(owner.getId()),
                    status,
                    rejectReason
            );
        }
    }


    public OwnershipTransfer approveStep(int id, String officerUsername, String officerRole, String signatureBase64, String sealBase64) {
        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new IllegalArgumentException("Signature is required");
        }
        if (sealBase64 == null || sealBase64.isBlank()) {
            throw new IllegalArgumentException("Seal is required");
        }

        OwnershipTransfer owner = ownerDAO.getById(id);
        if (owner == null) throw new IllegalArgumentException("Ownership transfer application not found");
        if (owner.getApprovalStage() == null) owner.setApprovalStage(0);
        String status = owner.getStatus() == null ? "Pending" : owner.getStatus().trim();

        if ("Pending".equalsIgnoreCase(status) || owner.getApprovalStage() == 0) {
            if (!"ROLE_Department_Officer".equals(officerRole)) {
                throw new IllegalStateException("First approval can only be completed by a Department Officer");
            }
            owner.setFirstApprovedBy(officerUsername);
            owner.setFirstApprovedAt(LocalDateTime.now());
            owner.setFirstSignature(signatureBase64);
            owner.setFirstSeal(sealBase64);
            owner.setApprovalStage(1);
            owner.setStatus("First Approved");
            return ownerDAO.update(owner);
        }

        if ("First Approved".equalsIgnoreCase(status) || owner.getApprovalStage() == 1) {
            boolean finalApprover = "ROLE_Admin_Municipal_Officer".equals(officerRole)
                    || "ROLE_Super_Admin".equals(officerRole);
            if (!finalApprover) {
                throw new IllegalStateException("Final approval can only be completed by an Admin or Super Admin");
            }
            if (officerUsername != null && officerUsername.equalsIgnoreCase(owner.getFirstApprovedBy())) {
                throw new IllegalStateException("Second approval must be completed by a different officer");
            }
            owner.setSecondApprovedBy(officerUsername);
            owner.setSecondApprovedAt(LocalDateTime.now());
            owner.setSecondSignature(signatureBase64);
            owner.setSecondSeal(sealBase64);
            owner.setApprovalStage(2);
            owner.setStatus("Approved");
            OwnershipTransfer updated = ownerDAO.update(owner);
            emailNotifier.sendStatusUpdate(owner.getContact(), owner.getCurrentOwner(), "মালিকানা হস্তান্তর", String.valueOf(owner.getId()), "Approved", "");
            return updated;
        }

        throw new IllegalStateException("Ownership transfer is already finally approved or cannot be approved");
    }

    private String saveFile(MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "file";
        String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.') + 1) : "bin";
        String filename   = prefix + UUID.randomUUID() + "." + ext;
        Path   uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
        Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return "uploads/" + filename;
    }
}
