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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.FamilyCard;
import com.mgt.service.FamilyCardService;
import com.mgt.service.FamilyCardPdfService;

@RestController
@RequestMapping("/api/family-card")
public class FamilyCardController {

    @Autowired
    FamilyCardService familyCardService;

    @Autowired
    FamilyCardPdfService pdfService;

    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    // ── APPLY ─────────────────────────────────────────────────────────────────

    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> apply(
            @RequestParam("holderName")                                          String holderName,
            @RequestParam("nid")                                                 String nid,
            @RequestParam(value = "dateOfBirth",       required = false)         String dateOfBirth,
            @RequestParam("contact")                                             String contact,
            @RequestParam("address")                                             String address,
            @RequestParam(value = "ward",              required = false)         String ward,
            @RequestParam(value = "unionName",         required = false)         String unionName,
            @RequestParam(value = "upazila",           required = false)         String upazila,
            @RequestParam(value = "district",          required = false)         String district,
            @RequestParam("membersCount")                                        int membersCount,
            @RequestParam(value = "incomeMonthly",     required = false)         String incomeMonthly,
            @RequestParam(value = "occupation",        required = false)         String occupation,
            @RequestParam(value = "husbandOrFatherName", required = false)       String husbandOrFatherName,
            @RequestParam(value = "hasOtherCard",      required = false, defaultValue = "false") String hasOtherCard,
            @RequestParam(value = "photo",             required = false)         MultipartFile photo,
            @RequestParam(value = "nidFile",           required = false)         MultipartFile nidFile
    ) { Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        try { Files.createDirectories(uploadPath); } catch (Exception ignored) {}

        String photoUrl   = null;
        String nidFileUrl = null;

        try {
            if (photo != null && !photo.isEmpty()) {
                String fn = "fc_photo_" + UUID.randomUUID() + "." + getExt(photo.getOriginalFilename());
                Files.copy(photo.getInputStream(), uploadPath.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                photoUrl = "uploads/" + fn;
            }
            if (nidFile != null && !nidFile.isEmpty()) {
                String fn = "fc_nid_" + UUID.randomUUID() + "." + getExt(nidFile.getOriginalFilename());
                Files.copy(nidFile.getInputStream(), uploadPath.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                nidFileUrl = "uploads/" + fn;
            }
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "ফাইল আপলোড ব্যর্থ: " + e.getMessage()));
        }

        FamilyCard card = new FamilyCard();
        card.setHolderName(holderName);
        card.setNid(nid);
        card.setDateOfBirth(dateOfBirth);
        card.setContact(contact);
        card.setAddress(address);
        card.setWard(ward);
        card.setUnionName(unionName);
        card.setUpazila(upazila);
        card.setDistrict(district);
        card.setMembersCount(membersCount);
        card.setIncomeMonthly(incomeMonthly);
        card.setOccupation(occupation);
        card.setHusbandOrFatherName(husbandOrFatherName);
        card.setHasOtherCard("true".equalsIgnoreCase(hasOtherCard));
        card.setPhotoUrl(photoUrl);
        card.setNidFileUrl(nidFileUrl);
        card.setCreatedAt(LocalDateTime.now());

        String result = familyCardService.create(card);
        if ("DUPLICATE".equals(result))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "এই NID দিয়ে আগেই আবেদন করা হয়েছে।"));

        return ResponseEntity.ok(Map.of(
                "message", "আবেদন সফলভাবে জমা হয়েছে।",
                "cardNo",  card.getCardNo() != null ? card.getCardNo() : ""
        ));
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @GetMapping("/getall")
    public List<FamilyCard> getAll() {
        return familyCardService.getAll();
    }

    @GetMapping("/status/{status}")
    public List<FamilyCard> getByStatus(@PathVariable String status) {
        return familyCardService.getByStatus(status);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        FamilyCard card = familyCardService.getById(id);
        if (card == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(card);
    }

    @GetMapping("/check/{nid}")
    public ResponseEntity<Object> checkByNid(@PathVariable String nid) {
        FamilyCard card = familyCardService.getByNid(nid);
        if (card == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of(
                "cardNo",          card.getCardNo()          != null ? card.getCardNo()          : "",
                "status",          card.getStatus()          != null ? card.getStatus()           : "",
                "holderName",      card.getHolderName()      != null ? card.getHolderName()       : "",
                "rejectionReason", card.getRejectionReason() != null ? card.getRejectionReason()  : ""
        ));
    }

    // ── UPDATE STATUS ─────────────────────────────────────────────────────────

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(
            @PathVariable int id,
            @RequestBody Map<String, String> body
    ) {
        String status = body.get("status");
        if (status == null || status.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "status আবশ্যক।"));

        familyCardService.updateStatus(
                id,
                status,
                body.getOrDefault("approvedBy", "Admin"),
                body.getOrDefault("rejectionReason", null),
                body.get("signatureBase64")
        );
        return ResponseEntity.ok(Map.of("message", "স্ট্যাটাস আপডেট হয়েছে।"));
    }

    // ── PDF DOWNLOAD ──────────────────────────────────────────────────────────
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadCard(@PathVariable int id) {
        FamilyCard card = familyCardService.getById(id);
        if (card == null)
            return ResponseEntity.notFound().build();

        byte[] pdf = pdfService.generateCard(card);
        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"FamilyCard_" + card.getCardNo() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        FamilyCard card = familyCardService.getById(id);
        if (card == null) return ResponseEntity.notFound().build();
        familyCardService.delete(id);
        return ResponseEntity.ok(Map.of("message", "মুছে ফেলা হয়েছে।"));
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private String getExt(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
