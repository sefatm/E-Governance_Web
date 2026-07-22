package com.mgt.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.BirthDeathCertificate;
import com.mgt.service.BirthDeathService;
import com.mgt.service.PdfService;

@RestController
@RequestMapping(value = "/api/birth-death")
public class BirthDeathController {

    @Autowired 
    BirthDeathService birthDeathService;
    
    @Autowired 
    PdfService pdfService;

    //private static final String UPLOAD_DIR = "uploads/";
    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    private String ext(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }

    private String saveFile(MultipartFile file, String prefix) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
        Files.createDirectories(uploadPath);

        String filename = prefix + "_" + UUID.randomUUID() + "." + ext(file.getOriginalFilename());
        Path dest = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        return "uploads/" + filename;
    }

    @PostMapping(value = "/create-birth", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createBirth(
            @RequestParam("name")                                       String name,
            @RequestParam(value="nameBn",            required=false)    String nameBn,
            @RequestParam("dob")                                        String dob,
            @RequestParam("placeOfBirth")                               String placeOfBirth,
            @RequestParam("genderOfBirth")                              String genderOfBirth,
            @RequestParam("address")                                    String address,
            @RequestParam(value="permanentAddress",  required=false)    String permanentAddress,
            @RequestParam("mobileNumber")                               String mobileNumber,
            @RequestParam(value="email",             required=false)    String email,
            @RequestParam("fathersName")                                String fathersName,
            @RequestParam(value="fathersDob",        required=false)    String fathersDob,
            @RequestParam("fathersNid")                                 String fathersNid,
            @RequestParam(value="fathersEmail",      required=false)    String fathersEmail,
            @RequestParam(value="fathersContact",    required=false)    String fathersContact,
            @RequestParam("mothersName")                                String mothersName,
            @RequestParam(value="mothersDob",        required=false)    String mothersDob,
            @RequestParam("mothersNid")                                 String mothersNid,
            @RequestParam(value="mothersEmail",      required=false)    String mothersEmail,
            @RequestParam(value="mothersContact",    required=false)    String mothersContact,
            @RequestParam(value="emergencyName",     required=false)    String emergencyName,
            @RequestParam(value="emergencyPhone",    required=false)    String emergencyPhone,
            @RequestParam("paymentMethod")                              String paymentMethod,
            @RequestParam("amount")                                     String amount,
            @RequestParam(value="fatherNid",         required=false)    MultipartFile fatherNidFile,
            @RequestParam(value="motherNid",         required=false)    MultipartFile motherNidFile,
            @RequestParam(value="vaccine",           required=false)    MultipartFile vaccineFile
    ) {
        try {
            BirthDeathCertificate cert = new BirthDeathCertificate();
            cert.setType("Birth");
            cert.setName(name);
            cert.setNameBn(nameBn);
            cert.setDob(dob);
            cert.setPlaceOfBirth(placeOfBirth);
            cert.setGenderOfBirth(genderOfBirth);
            cert.setAddress(address);
            cert.setPermanentAddress(permanentAddress);
            cert.setMobileNumber(mobileNumber);
            cert.setEmail(email);
            cert.setFathersName(fathersName);
            cert.setFathersDob(fathersDob);
            cert.setFathersNid(fathersNid);
            cert.setFathersEmail(fathersEmail);
            cert.setFathersContact(fathersContact);
            cert.setMothersName(mothersName);
            cert.setMothersDob(mothersDob);
            cert.setMothersNid(mothersNid);
            cert.setMothersEmail(mothersEmail);
            cert.setMothersContact(mothersContact);
            cert.setEmergencyName(emergencyName);
            cert.setEmergencyPhone(emergencyPhone);
            cert.setPaymentMethod(paymentMethod);
            cert.setAmount(amount);
            cert.setStatus("Pending");

            cert.setFatherNidFileUrl(saveFile(fatherNidFile, "bd_father_nid"));
            cert.setMotherNidFileUrl(saveFile(motherNidFile, "bd_mother_nid"));
            cert.setVaccineFileUrl  (saveFile(vaccineFile,   "bd_vaccine"));

            birthDeathService.create(cert);
            return ResponseEntity.ok(Map.of("message", "Birth certificate application submitted successfully"));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "File upload failed: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/create-death", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> createDeath(
            @RequestParam("name")                                       String name,
            @RequestParam(value="nameBn",            required=false)    String nameBn,
            @RequestParam(value="dob",               required=false)    String dob,
            @RequestParam("dateOfDeath")                                String dateOfDeath,
            @RequestParam("placeOfDeath")                               String placeOfDeath,
            @RequestParam("gender")                                     String gender,
            @RequestParam(value="birthNo",           required=false)    String birthNo,
            @RequestParam(value="nid",               required=false)    String nid,
            @RequestParam("address")                                    String address,
            @RequestParam(value="permanentAddress",  required=false)    String permanentAddress,
            @RequestParam("applicantName")                              String applicantName,
            @RequestParam("relation")                                   String relation,
            @RequestParam("mobileNumber")                               String mobileNumber,
            @RequestParam(value="email",             required=false)    String email,
            @RequestParam("paymentMethod")                              String paymentMethod,
            @RequestParam("amount")                                     String amount,
            @RequestParam(value="deathNid",          required=false)    MultipartFile deathNidFile,
            @RequestParam(value="medical",           required=false)    MultipartFile medicalFile
    ) {
        try {
            BirthDeathCertificate cert = new BirthDeathCertificate();
            cert.setType("Death");
            cert.setName(name);
            cert.setNameBn(nameBn);
            cert.setDob(dob);
            cert.setDateOfDeath(dateOfDeath);
            cert.setPlaceOfDeath(placeOfDeath);
            cert.setGender(gender);
            cert.setBirthNo(birthNo);
            cert.setNid(nid);
            cert.setAddress(address);
            cert.setPermanentAddress(permanentAddress);
            cert.setApplicantName(applicantName);
            cert.setRelation(relation);
            cert.setMobileNumber(mobileNumber);
            cert.setEmail(email);
            cert.setPaymentMethod(paymentMethod);
            cert.setAmount(amount);
            cert.setStatus("Pending");

            cert.setDeathNidFileUrl(saveFile(deathNidFile, "bd_death_nid"));
            cert.setMedicalFileUrl (saveFile(medicalFile,  "bd_medical"));

            birthDeathService.create(cert);
            return ResponseEntity.ok(Map.of("message", "Death certificate application submitted successfully"));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<BirthDeathCertificate> getall() {
        return birthDeathService.getall();
    }

    @GetMapping("/mobile/{mobile}")
    public List<BirthDeathCertificate> getByMobile(@PathVariable String mobile) {
        return birthDeathService.findByMobile(mobile);
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
            BirthDeathCertificate current = birthDeathService.getById(id);
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
        birthDeathService.updateStatus(id, requestedStatus);
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
            BirthDeathCertificate updated = birthDeathService.approveStep(id, officer, officerRole, body.get("signatureBase64"), body.get("sealBase64"));
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
            BirthDeathCertificate updated = birthDeathService.updateSeal(id, body.get("sealBase64"));
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
        BirthDeathCertificate cert = birthDeathService.ensureSealForDownload(id);
        if (cert == null) return ResponseEntity.notFound().build();

        String status = cert.getStatus();
        Integer stage = cert.getApprovalStage();
        if (status == null || !status.replace("\"", "").trim().equalsIgnoreCase("Approved")
                || stage == null || stage < 2) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        byte[] pdf;
        String filename;
        if ("Death".equalsIgnoreCase(cert.getType())) {
            pdf      = pdfService.generateDeathCertificate(cert);
            filename = "DeathCertificate_" + id + ".pdf";
        } else {
            pdf      = pdfService.generateBirthCertificate(cert);
            filename = "BirthCertificate_" + id + ".pdf";
        }

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
