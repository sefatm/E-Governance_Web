package com.mgt.controller;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import com.mgt.model.HoldingNewRegistration;
import com.mgt.service.HoldingNewRegistrationPdfService;
import com.mgt.service.HoldingNewService;
import com.mgt.service.WardService;
import com.mgt.model.Ward;

@RestController
@RequestMapping(value = "/api/holding-new-registration")
public class HoldingNewController {

    @Autowired 
    HoldingNewService hnService;
    
    @Autowired 
    HoldingNewRegistrationPdfService pdfService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WardService wardService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> request) {
        try {
            // Frontend sends ward as a number (for example: 5), while the entity
            // stores a Ward relation. Direct Jackson binding therefore failed before
            // entering this controller and Spring's protected /error endpoint masked
            // that binding error as HTTP 403. Resolve the relation explicitly.
            Map<String, Object> payload = new LinkedHashMap<>(request);
            Object wardValue = payload.remove("ward");

            HoldingNewRegistration holding = objectMapper.convertValue(
                    payload, HoldingNewRegistration.class);

            if (wardValue != null && !String.valueOf(wardValue).isBlank()) {
                int wardNumber;
                if (wardValue instanceof Number number) {
                    wardNumber = number.intValue();
                } else if (wardValue instanceof Map<?, ?> wardMap) {
                    Object numberValue = wardMap.get("number");
                    if (numberValue == null) numberValue = wardMap.get("wardNumber");
                    if (numberValue == null) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Valid ward number is required"));
                    }
                    wardNumber = Integer.parseInt(String.valueOf(numberValue));
                } else {
                    wardNumber = Integer.parseInt(String.valueOf(wardValue));
                }

                Ward ward = wardService.getByNumber(wardNumber);
                if (ward == null) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("message", "Ward " + wardNumber + " was not found"));
                }
                holding.setWard(ward);
            }

            HoldingNewRegistration saved = hnService.create(holding);
            return ResponseEntity.ok(Map.of(
                    "message", "Application submitted successfully",
                    "id", saved.getId()));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ward must be a valid number"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Application submission failed: " + e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public ResponseEntity<List<HoldingNewRegistration>> getall() {
        return ResponseEntity.ok(hnService.getall());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        HoldingNewRegistration h = hnService.getById(id);
        if (h == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(h);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body,
                                               Authentication authentication) {
        String requestedStatus = body.get("status");
        if ("Approved".equalsIgnoreCase(requestedStatus) || "First Approved".equalsIgnoreCase(requestedStatus)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Use the two-step approval endpoint"));
        }
        if ("Rejected".equalsIgnoreCase(requestedStatus)) {
            HoldingNewRegistration current = hnService.getById(id);
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
        hnService.updateStatus(id, requestedStatus);
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<Object> approveStep(@PathVariable int id, @RequestBody Map<String, String> body,
                                               Authentication authentication) {
        try {
            String officer = authentication == null ? "officer" : authentication.getName();
            HoldingNewRegistration updated = hnService.approveStep(id, officer, roleOf(authentication), body.get("signatureBase64"), body.get("sealBase64"));
            return ResponseEntity.ok(Map.of(
                    "message", updated.getApprovalStage() == 2 ? "Final approval completed" : "Department verification completed",
                    "status", updated.getStatus(),
                    "approvalStage", updated.getApprovalStage(),
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

    @PutMapping("/location/{id}")
    public ResponseEntity<Object> updateLocation(@PathVariable int id, @RequestBody Map<String, Double> body) {
        Double lat = body.get("latitude");
        Double lng = body.get("longitude");
        if (lat == null || lng == null)
            return ResponseEntity.badRequest().body(Map.of("message", "latitude and longitude required"));
        hnService.updateLocation(id, lat, lng);
        return ResponseEntity.ok(Map.of("message", "Location saved successfully"));
    }
    
    @PostMapping(value = "/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadDocuments(@PathVariable int id,
            @RequestParam(value = "nidFile",   required = false) MultipartFile nidFile,
            @RequestParam(value = "deedFile",  required = false) MultipartFile deedFile,
            @RequestParam(value = "photo",     required = false) MultipartFile photo) {
        try {
            HoldingNewRegistration h = hnService.getById(id);
            if (h == null) return ResponseEntity.notFound().build();

            Path uploadPath = Paths.get("src/main/resources/uploads").toAbsolutePath();
            Files.createDirectories(uploadPath);

            if (nidFile != null && !nidFile.isEmpty()) {
                String fn = "hold_nid_" + UUID.randomUUID() + getExt(nidFile);
                Files.copy(nidFile.getInputStream(), uploadPath.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                h.setNidFileUrl("uploads/" + fn);
            }
            if (deedFile != null && !deedFile.isEmpty()) {
                String fn = "hold_deed_" + UUID.randomUUID() + getExt(deedFile);
                Files.copy(deedFile.getInputStream(), uploadPath.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                h.setDeedFileUrl("uploads/" + fn);
            }
            if (photo != null && !photo.isEmpty()) {
                String fn = "hold_photo_" + UUID.randomUUID() + getExt(photo);
                Files.copy(photo.getInputStream(), uploadPath.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                h.setPhotoUrl("uploads/" + fn);
            }

            hnService.update(h);
            return ResponseEntity.ok(Map.of("message", "Documents uploaded successfully"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Upload failed: " + e.getMessage()));
        }
    }

    private String getExt(MultipartFile f) {
        String name = f.getOriginalFilename();
        return (name != null && name.contains(".")) ? name.substring(name.lastIndexOf('.')) : ".jpg";
    }

    @GetMapping("/generate-pdf/{id}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable int id) {
        HoldingNewRegistration holding = hnService.getById(id);
        if (holding == null) return ResponseEntity.notFound().build();
        if (!"Approved".equalsIgnoreCase(holding.getStatus()) || holding.getApprovalStage() == null || holding.getApprovalStage() < 2) {
            return ResponseEntity.status(403).build();
        }
        byte[] pdf = pdfService.generateCertificate(holding);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"holding-certificate-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
