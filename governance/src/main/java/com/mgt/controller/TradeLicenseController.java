package com.mgt.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.BusinessCategory;
import com.mgt.model.TradeLicenseApply;
import com.mgt.service.TradeLicensePdfService;
import com.mgt.service.TradeLicenseService;

/**
 * TradeLicenseController
 *
 * নতুন endpoints:
 *   GET /api/tradeLicense/categories        → Approved category list (Angular dropdown)
 *   GET /api/tradeLicense/categories/check  → একটা category valid কিনা check
 */
@RestController
@RequestMapping("/api/tradeLicense")
public class TradeLicenseController {

    @Autowired TradeLicenseService    tradelicenseservice;
    @Autowired TradeLicensePdfService pdfService;

    @Value("${citizen.upload.dir:src/main/resources/uploads/}")
    private String uploadDir;

    // ── নতুন: Approved Business Category List ────────────────────────────────

    /**
     * Angular dropdown এর জন্য সব approved business categories
     * GET /api/tradeLicense/categories
     *
     * Response example:
     * [
     *   { "key": "RESTAURANT", "nameEn": "Restaurant", "nameBn": "রেস্তোরাঁ",
     *     "baseFee": 5000.0, "requiresInspection": true },
     *   ...
     * ]
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        return ResponseEntity.ok(BusinessCategory.getAll());
    }

    /**
     * একটা business type valid কিনা check করো
     * GET /api/tradeLicense/categories/check?type=Restaurant
     *
     * Response: { "valid": true, "baseFee": 5000.0, "requiresInspection": true }
     */
    @GetMapping("/categories/check")
    public ResponseEntity<Object> checkCategory(@RequestParam String type) {
        boolean valid = BusinessCategory.isValid(type);
        if (!valid) {
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "message", "'" + type + "' অনুমোদিত category নয়।"
            ));
        }
        BusinessCategory cat = BusinessCategory.getByName(type);
        return ResponseEntity.ok(Map.of(
            "valid",              true,
            "nameEn",             cat.getNameEn(),
            "nameBn",             cat.getNameBn(),
            "baseFee",            cat.getBaseFee(),
            "requiresInspection", cat.isRequiresInspection()
        ));
    }

    // ─── Create (existing — category validation যোগ হয়েছে Service এ) ─────────
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> create(
            @RequestParam("businessName")   String businessName,
            @RequestParam("businessType")   String businessType,
            @RequestParam("licensePeriod")  Integer licensePeriod,
            @RequestParam("ownerName")      String ownerName,
            @RequestParam("fatherName")     String fatherName,
            @RequestParam("motherName")     String motherName,
            @RequestParam("dateOfBirth")    String dateOfBirth,
            @RequestParam("nid")            String nid,
            @RequestParam("mobile")         String mobile,
            @RequestParam(value = "email",  required = false) String email,
            @RequestParam("address")        String address,
            @RequestParam("wardNo")         String wardNo,
            @RequestParam("holdingNo")      String holdingNo,
            @RequestParam("income")         Double income,
            @RequestParam("tax")            Double tax,
            @RequestParam(value = "nidFile",       required = false) MultipartFile nidFile,
            @RequestParam(value = "photo",          required = false) MultipartFile photo,
            @RequestParam(value = "taxReceiptFile", required = false) MultipartFile taxReceiptFile
    ) {
        // Business category validation — Service এ হয়, error এলে 400 return হবে
        TradeLicenseApply trade = new TradeLicenseApply();
        trade.setBusinessName(businessName);
        trade.setBusinessType(businessType);
        trade.setLicensePeriod(licensePeriod);
        trade.setOwnerName(ownerName);
        trade.setFatherName(fatherName);
        trade.setMotherName(motherName);
        trade.setDateOfBirth(dateOfBirth);
        trade.setNid(nid);
        trade.setMobile(mobile);
        trade.setEmail(email);
        trade.setAddress(address);
        trade.setWardNo(wardNo);
        trade.setHoldingNo(holdingNo);
        trade.setIncome(income);
        trade.setTax(tax);
        trade.setNidFileUrl(saveFile(nidFile, "nid"));
        trade.setPhotoUrl(saveFile(photo, "photo"));
        trade.setTaxReceiptFileUrl(saveFile(taxReceiptFile, "tax"));
        trade.setAppliedDate(LocalDate.now());
        trade.setCreatedAt(LocalDateTime.now());

        try {
            TradeLicenseApply saved = tradelicenseservice.create(trade);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Existing endpoints ───────────────────────────────────────────────────

    @GetMapping("/getall")
    public ResponseEntity<List<TradeLicenseApply>> getAll() {
        return ResponseEntity.ok(tradelicenseservice.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        TradeLicenseApply t = tradelicenseservice.getById(id);
        if (t == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(t);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(
            @PathVariable int id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            String requestedStatus = body.get("status");
            if ("Approved".equalsIgnoreCase(requestedStatus) || "First Approved".equalsIgnoreCase(requestedStatus)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Use the two-step approval endpoint"));
            }
            if ("Rejected".equalsIgnoreCase(requestedStatus)) {
                TradeLicenseApply current = tradelicenseservice.getById(id);
                if (current == null) return ResponseEntity.badRequest().body(Map.of("message", "Application not found"));
                String role = roleOf(authentication);
                boolean isAdmin = "ROLE_Admin_Municipal_Officer".equals(role) || "ROLE_Super_Admin".equals(role);
                boolean isDept = "ROLE_Department_Officer".equals(role);
                if ("First Approved".equalsIgnoreCase(current.getStatus()) && !isAdmin) {
                    return ResponseEntity.status(403).body(Map.of("message", "After department verification, only Admin or Super Admin can reject"));
                }
                if ("Pending".equalsIgnoreCase(current.getStatus()) && !(isDept || isAdmin)) {
                    return ResponseEntity.status(403).body(Map.of("message", "Only Department Officer, Admin or Super Admin can reject"));
                }
            }
            tradelicenseservice.updateStatus(id, requestedStatus);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<Object> approveStep(@PathVariable int id, @RequestBody Map<String, String> body,
                                               Authentication authentication) {
        try {
            String officer = authentication == null ? "officer" : authentication.getName();
            TradeLicenseApply updated = tradelicenseservice.approveStep(id, officer, roleOf(authentication), body.get("signatureBase64"));
            return ResponseEntity.ok(Map.of(
                    "message", updated.getApprovalStage() == 2 ? "Final approval completed" : "Department verification completed",
                    "status", updated.getStatus(),
                    "approvalStage", updated.getApprovalStage()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    private String roleOf(Authentication authentication) {
        return authentication == null ? "" : authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).filter(a -> a.startsWith("ROLE_"))
                .findFirst().orElse("");
    }

    @PostMapping("/verify")
    public ResponseEntity<Object> verify(@RequestBody Map<String, String> body) {
        try {
            TradeLicenseApply t = tradelicenseservice.verify(
                body.get("licenseNumber"), body.get("birthDate"));
            return ResponseEntity.ok(t);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/certificate/{id}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable int id) {
        TradeLicenseApply trade = tradelicenseservice.getById(id);
        if (trade == null) return ResponseEntity.notFound().build();
        if (!"Approved".equalsIgnoreCase(trade.getStatus())) {
            return ResponseEntity.status(403).build();
        }
        trade = tradelicenseservice.ensureApprovedForDownload(trade);
        byte[] pdf = pdfService.generateTradeLicenseCertificate(trade);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"TradeLicense_" + id + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    // ─── File upload helper ───────────────────────────────────────────────────
    private String saveFile(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) return null;
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);
            String ext      = getExtension(file.getOriginalFilename());
            String filename = prefix + "_" + UUID.randomUUID() + "." + ext;
            Files.copy(file.getInputStream(), uploadPath.resolve(filename),
                StandardCopyOption.REPLACE_EXISTING);
            return "uploads/" + filename;
        } catch (IOException e) {
            return null;
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
