package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.OwnershipTransfer;
import com.mgt.service.OwnershipTransferPdfService;
import com.mgt.service.OwnershipTransferService;

@RestController
@RequestMapping("/api/ownership-transfer")
public class OwnershipTransferController {

    @Autowired
    OwnershipTransferService ownershipService;

    @Autowired
    OwnershipTransferPdfService pdfService;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> create(
            @RequestParam("currentOwner")    String currentOwner,
            @RequestParam("currentOwnerNid") String currentOwnerNid,
            @RequestParam("newOwner")        String newOwner,
            @RequestParam("newOwnerNid")     String newOwnerNid,
            @RequestParam("contact")         String contact,
            @RequestParam(value = "relationship", required = false) String relationship,
            @RequestParam("holdingNumber")   String holdingNumber,
            @RequestParam(value = "wardNo",  required = false) String wardNo,
            @RequestParam("address")         String address,
            @RequestParam("reason")          String reason,
            @RequestParam(value = "currentOwnerNidFile", required = false) MultipartFile currentOwnerNidFile,
            @RequestParam(value = "newOwnerNidFile",     required = false) MultipartFile newOwnerNidFile,
            @RequestParam(value = "deedFile",            required = false) MultipartFile deedFile
    ) {
        try {
            OwnershipTransfer owner = new OwnershipTransfer();
            owner.setCurrentOwner(currentOwner);
            owner.setCurrentOwnerNid(currentOwnerNid);
            owner.setNewOwner(newOwner);
            owner.setNewOwnerNid(newOwnerNid);
            owner.setContact(contact);
            owner.setRelationship(relationship);
            owner.setHoldingNumber(holdingNumber);
            owner.setWardNo(wardNo);
            owner.setAddress(address);
            owner.setReason(reason);
            owner.setStatus("Pending");

            ownershipService.create(owner, currentOwnerNidFile, newOwnerNidFile, deedFile);

            return ResponseEntity.ok(Map.of("message", "Application submitted successfully"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // GET /getall 
    @GetMapping("/getall")
    public ResponseEntity<List<OwnershipTransfer>> getall() {
        return ResponseEntity.ok(ownershipService.getall());
    }

    // GET /{id} 
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        OwnershipTransfer o = ownershipService.getById(id);
        if (o == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(o);
    }

    // PUT /status/{id} 
    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        String status       = body.get("status");
        String rejectReason = body.getOrDefault("rejectReason", "");
        ownershipService.updateStatus(id, status, rejectReason);
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }


    @PutMapping("/approve/{id}")
    public ResponseEntity<Object> approveStep(@PathVariable int id,
                                               @RequestBody Map<String, String> body,
                                               Authentication authentication) {
        try {
            String officer = authentication == null ? "officer" : authentication.getName();
            OwnershipTransfer updated = ownershipService.approveStep(
                    id, officer, roleOf(authentication), body.get("signatureBase64"), body.get("sealBase64"));
            return ResponseEntity.ok(Map.of(
                    "message", updated.getApprovalStage() != null && updated.getApprovalStage() == 2
                            ? "Final ownership transfer approval completed"
                            : "Department ownership transfer verification completed",
                    "status", updated.getStatus(),
                    "approvalStage", updated.getApprovalStage(),
                    "signatureSaved", body.get("signatureBase64") != null && !body.get("signatureBase64").isBlank(),
                    "sealSaved", body.get("sealBase64") != null && !body.get("sealBase64").isBlank()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    private String roleOf(Authentication authentication) {
        return authentication == null ? "" : authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).filter(a -> a.startsWith("ROLE_"))
                .findFirst().orElse("");
    }

    // GET /generate-pdf/{id} 
    @GetMapping("/generate-pdf/{id}")
    public ResponseEntity<?> generatePdf(@PathVariable int id) {
        OwnershipTransfer transfer = ownershipService.getById(id);
        if (transfer == null) return ResponseEntity.notFound().build();
        if (!"Approved".equalsIgnoreCase(transfer.getStatus()) || transfer.getApprovalStage() == null || transfer.getApprovalStage() < 2) {
            return ResponseEntity.status(409).body(Map.of("message", "Ownership transfer final approval is not complete yet."));
        }

        byte[] pdf = pdfService.generateCertificate(transfer);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"ownership-transfer-certificate-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
