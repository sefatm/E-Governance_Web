package com.mgt.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.CitizenCertificate;
import com.mgt.service.CitizenCertificatePdfService;
import com.mgt.service.CitizenService;

@RestController
@RequestMapping("/api/citizen")
public class CitizenController {

    @Autowired
    CitizenService citizenService;

    @Autowired
    CitizenCertificatePdfService pdfService;

    //private static final String UPLOAD_DIR = "uploads/";
    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> create(
            @RequestParam("name")             String name,
            @RequestParam("fatherName")       String fatherName,
            @RequestParam("motherName")       String motherName,
            @RequestParam("nid")              String nid,
            @RequestParam("dateOfBirth")      String dateOfBirth,
            @RequestParam("gender")           String gender,
            @RequestParam(value="bloodGroup",     required=false) String bloodGroup,
            @RequestParam(value="religion",       required=false) String religion,
            @RequestParam(value="maritalStatus",  required=false) String maritalStatus,
            @RequestParam(value="occupation",     required=false) String occupation,
            @RequestParam("contact")          String contact,
            @RequestParam(value="email",          required=false) String email,
            @RequestParam("address")          String address,
            @RequestParam(value="permanentAddress", required=false) String permanentAddress,
            @RequestParam("division")         String division,
            @RequestParam("district")         String district,
            @RequestParam("certificateType")  String certificateType,
            @RequestParam("purpose")          String purpose,
            @RequestParam("declaration")      String declaration,
            @RequestParam(value="photo",    required=false) MultipartFile photo,
            @RequestParam(value="nidFile",  required=false) MultipartFile nidFile
    ) {
        // ── Check for duplicate NID before processing files ──────────────────
        if (citizenService.existsByNid(nid)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "এই NID দিয়ে ইতিমধ্যে আবেদন জমা আছে। একই NID দিয়ে দুটি আবেদন করা যাবে না।"));
        }

        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
        try { java.nio.file.Files.createDirectories(uploadPath); } catch (Exception ignored) {}

        String photoUrl   = null;
        String nidFileUrl = null;

        try {
            if (photo != null && !photo.isEmpty()) {
                String ext      = getExtension(photo.getOriginalFilename());
                String filename = "photo_" + UUID.randomUUID() + "." + ext;
                Files.copy(photo.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                photoUrl = "uploads/" + filename;
            }

            if (nidFile != null && !nidFile.isEmpty()) {
                String ext      = getExtension(nidFile.getOriginalFilename());
                String filename = "nid_" + UUID.randomUUID() + "." + ext;
                Files.copy(nidFile.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                nidFileUrl = "uploads/" + filename;
            }

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "File upload failed: " + e.getMessage()));
        }

        CitizenCertificate c = new CitizenCertificate();
        c.setName(name);
        c.setFatherName(fatherName);
        c.setMotherName(motherName);
        c.setNid(nid);
        c.setDateOfBirth(dateOfBirth);
        c.setGender(gender);
        c.setBloodGroup(bloodGroup);
        c.setReligion(religion);
        c.setMaritalStatus(maritalStatus);
        c.setOccupation(occupation);
        c.setContact(contact);
        c.setEmail(email);
        c.setAddress(address);
        c.setPermanentAddress(permanentAddress);
        c.setDivision(division);
        c.setDistrict(district);
        c.setCertificateType(certificateType);
        c.setPurpose(purpose);
        c.setDeclaration("true".equalsIgnoreCase(declaration));
        c.setPhotoUrl(photoUrl);
        c.setNidFileUrl(nidFileUrl);
        c.setStatus("Pending");
        c.setCreatedAt(LocalDateTime.now());

        String trackingNo = "CC-" + LocalDateTime.now().getYear()
                + "-" + String.format("%05d", (int)(Math.random() * 90000) + 10000);
        c.setCertificateNo(trackingNo);

        try {
            citizenService.create(c);
        } catch (Exception e) {
            String msg = e.getMessage() != null && e.getMessage().contains("nid")
                    ? "এই NID দিয়ে ইতিমধ্যে আবেদন জমা আছে।"
                    : "আবেদন সংরক্ষণে সমস্যা হয়েছে। আবার চেষ্টা করুন।";
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }

        return ResponseEntity.ok(Map.of(
            "message",    "Application submitted successfully",
            "trackingNo", trackingNo
        ));
    }

    @GetMapping("/getall")
    public List<CitizenCertificate> getAll() {
        return citizenService.getall();
    }

    // ── Citizen নিজের application mobile দিয়ে খুঁজবে (status page) ──────────
    @GetMapping("/by-contact/{mobile}")
    public List<CitizenCertificate> getByContact(@PathVariable String mobile) {
        return citizenService.getByContact(mobile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        CitizenCertificate c = citizenService.getById(id);
        if (c == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(c);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body,
                                               Authentication authentication) {
        String requestedStatus = body.get("status");
        if ("Approved".equalsIgnoreCase(requestedStatus) || "First Approved".equalsIgnoreCase(requestedStatus)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Use the two-step approval endpoint for certificate approval"));
        }
        if ("Rejected".equalsIgnoreCase(requestedStatus)) {
            String role = authentication == null ? "" : authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .filter(a -> a.startsWith("ROLE_"))
                    .findFirst()
                    .orElse("");
            CitizenCertificate current = citizenService.getById(id);
            if (current == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Application not found"));
            }
            boolean isAdmin = "ROLE_Admin_Municipal_Officer".equals(role) || "ROLE_Super_Admin".equals(role);
            boolean isDept = "ROLE_Department_Officer".equals(role);
            if ("First Approved".equalsIgnoreCase(current.getStatus()) && !isAdmin) {
                return ResponseEntity.status(403).body(Map.of("message", "After department verification, only Admin or Super Admin can reject this application"));
            }
            if ("Pending".equalsIgnoreCase(current.getStatus()) && !(isDept || isAdmin)) {
                return ResponseEntity.status(403).body(Map.of("message", "Only Department Officer, Admin or Super Admin can reject a pending certificate application"));
            }
        }
        citizenService.updateStatus(id, requestedStatus);
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<Object> approveStep(@PathVariable int id,
                                               @RequestBody Map<String, String> body,
                                               Authentication authentication) {
        try {
            String officer = authentication == null ? "officer" : authentication.getName();
            String officerRole = authentication == null ? "" : authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .filter(a -> a.startsWith("ROLE_"))
                    .findFirst()
                    .orElse("");
            CitizenCertificate updated = citizenService.approveStep(id, officer, officerRole, body.get("signatureBase64"), body.get("sealBase64"));
            return ResponseEntity.ok(Map.of(
                "message", updated.getApprovalStage() == 2 ? "Final approval completed" : "First approval completed",
                "status", updated.getStatus(),
                "approvalStage", updated.getApprovalStage()
            ));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @PutMapping("/seal/{id}")
    public ResponseEntity<Object> updateSeal(@PathVariable int id, @RequestBody Map<String, String> body) {
        try {
            CitizenCertificate updated = citizenService.updateSeal(id, body.get("sealBase64"));
            return ResponseEntity.ok(Map.of(
                "message", "Seal updated successfully",
                "status", updated.getStatus(),
                "approvalStage", updated.getApprovalStage()
            ));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable int id) {
        CitizenCertificate c = citizenService.ensureSealForDownload(id);
        if (c == null) return ResponseEntity.notFound().build();
        if (!"Approved".equalsIgnoreCase(c.getStatus())
                || c.getApprovalStage() == null || c.getApprovalStage() < 2)
            return ResponseEntity.status(HttpStatus.CONFLICT).build();

        byte[] pdf = pdfService.generateCitizenCertificate(c);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"CitizenCertificate_" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/generate-pdf/{id}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable int id) {
        return download(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        citizenService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
    
}
