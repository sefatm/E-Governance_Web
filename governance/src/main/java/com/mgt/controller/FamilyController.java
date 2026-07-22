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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.FamilyCertificate;
import com.mgt.service.FamilyCertificatePdfService;
import com.mgt.service.FamilyService;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @Autowired
    private FamilyService fservice;

    @Autowired
    private FamilyCertificatePdfService pdfService;

    //private static final String UPLOAD_DIR = "uploads/";
    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    //  POST /api/family/create 
    @PostMapping("/create")
    public void create(@RequestBody FamilyCertificate family) {
        fservice.create(family);
    }

    // POST /api/family/create-multipart
    @PostMapping(value = "/create-multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createMultipart(
            @RequestParam("headName")               String headName,
            @RequestParam("nid")                    String nid,
            @RequestParam("contact")                String contact,
            @RequestParam("address")                String address,
            @RequestParam(value="permanentAddress", required=false) String permanentAddress,
            @RequestParam(value="division",         required=false) String division,
            @RequestParam(value="district",         required=false) String district,
            @RequestParam("purpose")                String purpose,
            @RequestParam("memberCount")            int memberCount,
            @RequestParam("membersJson")            String membersJson,
            @RequestParam(value="headPhoto",    required=false) MultipartFile headPhoto,
            @RequestParam(value="headNidFile",  required=false) MultipartFile headNidFile,
            @RequestParam Map<String, MultipartFile> allParams
    ) {
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
        try { java.nio.file.Files.createDirectories(uploadPath); } catch (Exception ignored) {}

        String headPhotoUrl = null;
        String headNidUrl   = null;
        java.util.List<String> docUrlList = new java.util.ArrayList<>();

        try {
            if (headPhoto != null && !headPhoto.isEmpty()) {
                String fn = "fam_photo_" + UUID.randomUUID() + "." + ext(headPhoto.getOriginalFilename());
                Files.copy(headPhoto.getInputStream(), uploadPath.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                headPhotoUrl = "uploads/" + fn;
            }
            if (headNidFile != null && !headNidFile.isEmpty()) {
                String fn = "fam_nid_" + UUID.randomUUID() + "." + ext(headNidFile.getOriginalFilename());
                Files.copy(headNidFile.getInputStream(), uploadPath.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                headNidUrl = "uploads/" + fn;
            }

            for (int i = 0; ; i++) {
                MultipartFile mf = allParams.get("memberDoc_" + i);
                if (mf == null) break;
                if (!mf.isEmpty()) {
                    String fn = "mem_doc_" + i + "_" + UUID.randomUUID() + "." + ext(mf.getOriginalFilename());
                    Files.copy(mf.getInputStream(), uploadPath.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                    docUrlList.add("uploads/" + fn);
                } else {
                    docUrlList.add(null);
                }
            }

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "File upload failed: " + e.getMessage()));
        }

        FamilyCertificate f = new FamilyCertificate();
        f.setHeadName(headName);
        f.setNid(nid);
        f.setContact(contact);
        f.setAddress(address);
        f.setPermanentAddress(permanentAddress);
        f.setDivision(division);
        f.setDistrict(district);
        f.setPurpose(purpose);
        f.setMemberCount(memberCount);

        String updatedMembersJson = mergeMemberDocUrls(membersJson, docUrlList);
        f.setMembersJson(updatedMembersJson);

        String allDocUrls = docUrlList.stream()
            .filter(u -> u != null)
            .collect(java.util.stream.Collectors.joining(","));
        f.setMemberDocUrls(allDocUrls.isEmpty() ? null : allDocUrls);

        f.setHeadPhotoUrl(headPhotoUrl);
        f.setHeadNidUrl(headNidUrl);
        f.setStatus("Pending");
        f.setCreatedAt(LocalDateTime.now());

        String trackingNo = "FAM-" + LocalDateTime.now().getYear()
                + "-" + String.format("%05d", (int)(Math.random() * 90000) + 10000);
        f.setCertificateNo(trackingNo);

        fservice.create(f);

        return ResponseEntity.ok(Map.of(
            "message",    "Application submitted successfully",
            "trackingNo", trackingNo
        ));
    }

    // GET /api/family/getall
    @GetMapping("/getall")
    public List<FamilyCertificate> getAll() {
        return fservice.getAll();
    }

    // GET /api/family/{id}
    @GetMapping("/mobile/{mobile}")
    public List<FamilyCertificate> getByMobile(@PathVariable String mobile) {
        return fservice.findByContact(mobile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FamilyCertificate> getById(@PathVariable int id) {
        FamilyCertificate f = fservice.getById(id);
        if (f == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(f);
    }

    // PUT /api/family/update/{id} 
    @PutMapping("/update/{id}")
    public void update(@PathVariable int id, @RequestBody FamilyCertificate family) {
        fservice.update(id, family);
    }

    // PUT /api/family/status/{id}
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
            FamilyCertificate current = fservice.getById(id);
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
        fservice.updateStatus(id, requestedStatus);
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    // GET /api/family/generate-pdf/{id} 
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
            FamilyCertificate updated = fservice.approveStep(id, officer, officerRole, body.get("signatureBase64"), body.get("sealBase64"));
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
            FamilyCertificate updated = fservice.updateSeal(id, body.get("sealBase64"));
            return ResponseEntity.ok(Map.of(
                "message", "Seal updated successfully",
                "status", updated.getStatus(),
                "approvalStage", updated.getApprovalStage()
            ));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        }
    }

    @GetMapping("/generate-pdf/{id}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable int id) {
        FamilyCertificate f = fservice.ensureSealForDownload(id);
        if (f == null) return ResponseEntity.notFound().build();
        if (!"Approved".equalsIgnoreCase(f.getStatus()) || (f.getApprovalStage() == null || f.getApprovalStage() < 2))
            return ResponseEntity.badRequest().build();

        byte[] pdf      = pdfService.generateFamilyCertificate(f);
        String filename = "family-certificate-" + id + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // DELETE /api/family/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        fservice.delete(id);
    }

    private String ext(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    @SuppressWarnings("unchecked")
    private String mergeMemberDocUrls(String membersJson, java.util.List<String> docUrls) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.List<java.util.Map<String, Object>> members =
                mapper.readValue(membersJson, new com.fasterxml.jackson.core.type.TypeReference<>() {});
            for (int i = 0; i < members.size(); i++) {
                if (i < docUrls.size() && docUrls.get(i) != null) {
                    members.get(i).put("docUrl", docUrls.get(i));
                }
            }
            return mapper.writeValueAsString(members);
        } catch (Exception e) {
            return membersJson; 
        }
    }
}
