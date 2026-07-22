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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.dao.TradeLicenseDAO;
import com.mgt.model.TradeLicenseApply;
import com.mgt.model.TradeRenewal;
import com.mgt.service.TradeLicensePdfService;
import com.mgt.service.TradeLicenseService;
import com.mgt.service.TradeRenewalService;

/**
 * TradeRenewalController
 *
 * নতুন endpoint:
 *   GET /api/trade-renewal/fine-check/{licenseId}
 *       → Renewal করার আগে late fine কত হবে সেটা Angular কে দেখাবে
 */
@RestController
@RequestMapping("/api/trade-renewal")
public class TradeRenewalController {

    @Autowired TradeRenewalService  tradeRenewalService;
    @Autowired TradeLicensePdfService pdfService;
    @Autowired TradeLicenseDAO      tradeLicenseDAO;
    @Autowired TradeLicenseService  tradeLicenseService;

    @Value("${citizen.upload.dir:src/main/resources/uploads/}")
    private String uploadDir;

    // ── নতুন: Late Fine Preview ───────────────────────────────────────────────
    /**
     * Renewal form submit করার আগে Angular এ fine কত হবে দেখাবে
     * GET /api/trade-renewal/fine-check/{licenseId}
     *
     * Response:
     * {
     *   "hasLateFine": true,
     *   "fineAmount": 1500.0,
     *   "monthsLate": 3,
     *   "expiryDate": "2024-12-31",
     *   "message": "আপনার License ৩ মাস আগে expire হয়েছে। Late Fine: ৳ ১,৫০০"
     * }
     */
    @GetMapping("/fine-check/{licenseId}")
    public ResponseEntity<Object> checkLateFine(@PathVariable int licenseId) {
        TradeLicenseApply license = tradeLicenseDAO.getById(licenseId);
        if (license == null)
            return ResponseEntity.notFound().build();

        if (license.getExpiryDate() == null) {
            return ResponseEntity.ok(Map.of(
                "hasLateFine", false,
                "fineAmount",  0.0,
                "message",     "License এর expiry তারিখ নেই।"
            ));
        }

        java.time.LocalDate today = java.time.LocalDate.now();
        if (!today.isAfter(license.getExpiryDate())) {
            return ResponseEntity.ok(Map.of(
                "hasLateFine", false,
                "fineAmount",  0.0,
                "expiryDate",  license.getExpiryDate().toString(),
                "message",     "License এখনো valid। কোনো Late Fine নেই।"
            ));
        }

        long monthsLate = java.time.temporal.ChronoUnit.MONTHS
            .between(license.getExpiryDate(), today);
        double baseTax  = license.getTax() != null ? license.getTax() : 0.0;
        double fineRate = Math.min(monthsLate * 0.05, 0.50);
        double fine     = Math.round(baseTax * fineRate);

        return ResponseEntity.ok(Map.of(
            "hasLateFine", true,
            "fineAmount",  fine,
            "monthsLate",  monthsLate,
            "expiryDate",  license.getExpiryDate().toString(),
            "message",     "আপনার License " + monthsLate + " মাস আগে expire হয়েছে। Late Fine: ৳ " +
                           String.format("%,.0f", fine)
        ));
    }

    // ─── Create Renewal ───────────────────────────────────────────────────────
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> create(
            @RequestParam("licenseNumber")    String licenseNumber,
            @RequestParam("licenseExpiry")    String licenseExpiry,
            @RequestParam("issuingAuthority") String issuingAuthority,
            @RequestParam("businessName")     String businessName,
            @RequestParam("businessType")     String businessType,
            @RequestParam("address")          String address,
            @RequestParam("wardNo")           String wardNo,
            @RequestParam("holdingNo")        String holdingNo,
            @RequestParam("applicantName")    String applicantName,
            @RequestParam("fatherName")       String fatherName,
            @RequestParam("motherName")       String motherName,
            @RequestParam("dateOfBirth")      String dateOfBirth,
            @RequestParam("nid")              String nid,
            @RequestParam("contact")          String contact,
            @RequestParam(value = "email",    required = false) String email,
            @RequestParam("renewalPeriod")    int renewalPeriod,
            @RequestParam("annualIncome")     double annualIncome,
            @RequestParam("taxPaid")          double taxPaid,
            @RequestParam("purpose")          String purpose,
            @RequestParam(value = "declaration",  defaultValue = "false") String declaration,
            @RequestParam(value = "nidFile",      required = false) MultipartFile nidFile,
            @RequestParam(value = "photo",         required = false) MultipartFile photo,
            @RequestParam(value = "licenseFile",   required = false) MultipartFile licenseFile
    ) {
        TradeLicenseApply license = tradeLicenseDAO.findByLicenseNumber(licenseNumber);
        if (license == null)
            return ResponseEntity.badRequest()
                .body(Map.of("message", "License Number পাওয়া যায়নি: " + licenseNumber));

        TradeRenewal renewal = new TradeRenewal();
        renewal.setOriginalLicense(license);
        renewal.setLicenseExpiry(licenseExpiry);
        renewal.setIssuingAuthority(issuingAuthority);
        renewal.setBusinessName(businessName);
        renewal.setBusinessType(businessType);
        renewal.setAddress(address);
        renewal.setWardNo(wardNo);
        renewal.setHoldingNo(holdingNo);
        renewal.setApplicantName(applicantName);
        renewal.setFatherName(fatherName);
        renewal.setMotherName(motherName);
        renewal.setDateOfBirth(dateOfBirth);
        renewal.setNid(nid);
        renewal.setContact(contact);
        renewal.setEmail(email);
        renewal.setRenewalPeriod(renewalPeriod);
        renewal.setAnnualIncome(annualIncome);
        renewal.setTaxPaid(taxPaid);
        renewal.setPurpose(purpose);
        renewal.setDeclaration("true".equalsIgnoreCase(declaration));
        renewal.setNidFileUrl(saveFile(nidFile,     "rnid"));
        renewal.setPhotoUrl(saveFile(photo,          "rphoto"));
        renewal.setLicenseFileUrl(saveFile(licenseFile, "rlicense"));
        renewal.setCreatedAt(LocalDateTime.now());

        try {
            TradeRenewal saved = tradeRenewalService.create(renewal);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Existing endpoints ───────────────────────────────────────────────────

    @GetMapping("/getall")
    public ResponseEntity<List<TradeRenewal>> getAll() {
        return ResponseEntity.ok(tradeRenewalService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        TradeRenewal t = tradeRenewalService.getById(id);
        if (t == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(t);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        try {
            tradeRenewalService.updateStatus(id, body.get("status"));
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    @PutMapping("/approve/{id}")
    public ResponseEntity<Object> approveStep(@PathVariable int id,
                                               @RequestBody Map<String, String> body,
                                               Authentication authentication) {
        try {
            String officer = authentication == null ? "officer" : authentication.getName();
            TradeRenewal updated = tradeRenewalService.approveStep(id, officer, roleOf(authentication), body.get("signatureBase64"));
            return ResponseEntity.ok(Map.of(
                    "message", updated.getApprovalStage() != null && updated.getApprovalStage() == 2
                            ? "Final renewal approval completed"
                            : "Department renewal verification completed",
                    "status", updated.getStatus(),
                    "approvalStage", updated.getApprovalStage(),
                    "signatureSaved", body.get("signatureBase64") != null && !body.get("signatureBase64").isBlank()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    private String roleOf(Authentication authentication) {
        return authentication == null ? "" : authentication.getAuthorities().stream()
                .map(a -> a.getAuthority()).filter(a -> a.startsWith("ROLE_"))
                .findFirst().orElse("");
    }

    @GetMapping("/certificate/{id}")
    public ResponseEntity<?> downloadCertificate(@PathVariable int id) {
        TradeRenewal renewal = tradeRenewalService.getById(id);
        if (renewal == null) return ResponseEntity.notFound().build();
        if (!"Approved".equalsIgnoreCase(renewal.getStatus())) {
            return ResponseEntity.status(409).body(Map.of("message", "Renewal final approval is not complete yet."));
        }
        renewal = tradeRenewalService.ensureApprovedForDownload(renewal);
        byte[] pdf = pdfService.generateRenewalCertificate(renewal);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"TradeRenewal_" + id + ".pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }

    // ─── File helper ──────────────────────────────────────────────────────────
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
