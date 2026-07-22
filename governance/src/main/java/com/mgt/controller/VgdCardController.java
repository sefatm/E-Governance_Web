package com.mgt.controller;

import com.mgt.model.VgdCard;
import com.mgt.service.VgdCardPdfService;
import com.mgt.service.VgdCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/vgd-card")
public class VgdCardController {

    @Autowired VgdCardService    svc;
    @Autowired VgdCardPdfService pdfService;

    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    // ── APPLY ─────────────────────────────────────────────────
    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> apply(
            @RequestParam("cardType")                                                    String cardType,
            @RequestParam("holderName")                                                  String holderName,
            @RequestParam("nid")                                                         String nid,
            @RequestParam(value="dateOfBirth",     required=false)                       String dateOfBirth,
            @RequestParam(value="contact",         required=false)                       String contact,
            @RequestParam(value="husbandName",     required=false)                       String husbandName,
            @RequestParam(value="fatherName",      required=false)                       String fatherName,
            @RequestParam("address")                                                     String address,
            @RequestParam(value="ward",            required=false)                       String ward,
            @RequestParam(value="unionName",       required=false)                       String unionName,
            @RequestParam(value="upazila",         required=false)                       String upazila,
            @RequestParam(value="district",        required=false)                       String district,
            @RequestParam(value="maritalStatus",   required=false)                       String maritalStatus,
            @RequestParam(value="disability",      required=false)                       String disability,
            @RequestParam(value="hasLand",         required=false, defaultValue="false") String hasLand,
            @RequestParam(value="landArea",        required=false, defaultValue="0")     String landArea,
            @RequestParam(value="incomeMonthly",   required=false)                       String incomeMonthly,
            @RequestParam(value="membersCount",    required=false, defaultValue="1")     int membersCount,
            @RequestParam(value="childrenCount",   required=false, defaultValue="0")     int childrenCount,
            @RequestParam(value="hasOtherCard",    required=false, defaultValue="false") String hasOtherCard,
            @RequestParam(value="bankName",        required=false)                       String bankName,
            @RequestParam(value="bankAccount",     required=false)                       String bankAccount,
            @RequestParam(value="mobileBanking",   required=false)                       String mobileBanking,
            @RequestParam(value="mobileBankingNo", required=false)                       String mobileBankingNo,
            @RequestParam(value="photo",           required=false)                       MultipartFile photo,
            @RequestParam(value="nidFile",         required=false)                       MultipartFile nidFile
    ) {
        Path up = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        try { Files.createDirectories(up); } catch (Exception ignored) {}

        String photoUrl = null, nidUrl = null;
        try {
            if (photo   != null && !photo.isEmpty())   photoUrl = saveFile(photo,   "vgd_photo_", up);
            if (nidFile != null && !nidFile.isEmpty()) nidUrl   = saveFile(nidFile, "vgd_nid_",   up);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "ফাইল আপলোড ব্যর্থ: " + e.getMessage()));
        }

        VgdCard card = new VgdCard();
        card.setCardType(cardType.toUpperCase());
        card.setHolderName(holderName);
        card.setNid(nid);
        card.setDateOfBirth(dateOfBirth);
        card.setContact(contact);
        card.setHusbandName(husbandName);
        card.setFatherName(fatherName);
        card.setAddress(address);
        card.setWard(ward);
        card.setUnionName(unionName);
        card.setUpazila(upazila);
        card.setDistrict(district);
        card.setMaritalStatus(maritalStatus);
        card.setDisability(disability);
        card.setHasLand("true".equalsIgnoreCase(hasLand));
        card.setLandArea(dec(landArea));
        card.setIncomeMonthly(incomeMonthly);
        card.setMembersCount(membersCount);
        card.setChildrenCount(childrenCount);
        // ✅ FIX Bug 12: hasOtherCard now saved
        card.setHasOtherCard("true".equalsIgnoreCase(hasOtherCard));
        card.setBankName(bankName);
        card.setBankAccount(bankAccount);
        card.setMobileBanking(mobileBanking);
        card.setMobileBankingNo(mobileBankingNo);
        card.setPhotoUrl(photoUrl);
        card.setNidFileUrl(nidUrl);
        card.setCreatedAt(LocalDateTime.now());

        return switch (svc.create(card)) {
            case "LAND_EXCEEDED" -> ResponseEntity.badRequest()
                    .body(Map.of("message", "০.৫ একরের বেশি জমি থাকলে VGD/VGF কার্ডের জন্য যোগ্য নন।"));
            case "DUPLICATE" -> ResponseEntity.badRequest()
                    .body(Map.of("message", "এই NID দিয়ে আগেই আবেদন করা হয়েছে।"));
            default -> ResponseEntity.ok(Map.of(
                    "message", card.getCardType() + " কার্ডের আবেদন সফলভাবে জমা হয়েছে।",
                    "cardNo",  card.getCardNo() != null ? card.getCardNo() : ""
            ));
        };
    }

    // ── READ ──────────────────────────────────────────────────
    @GetMapping("/getall")
    public List<VgdCard> getAll() { return svc.getAll(); }

    @GetMapping("/status/{status}")
    public List<VgdCard> getByStatus(@PathVariable String status) { return svc.getByStatus(status); }

    @GetMapping("/type/{cardType}")
    public List<VgdCard> getByCardType(@PathVariable String cardType) { return svc.getByCardType(cardType); }

    @GetMapping("/ward/{ward}")
    public List<VgdCard> getByWard(@PathVariable String ward) { return svc.getByWard(ward); }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        VgdCard c = svc.getById(id);
        return c != null ? ResponseEntity.ok(c) : ResponseEntity.notFound().build();
    }

    /**
     * ✅ FIX Bug 7: id field added to checkByNid response.
     * Previously missing → frontend resultId=null → downloadUrl=/download/null → 404
     */
    @GetMapping("/check/{nid}")
    public ResponseEntity<Object> checkByNid(@PathVariable String nid) {
        VgdCard c = svc.getByNid(nid);
        if (c == null) return ResponseEntity.notFound().build();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("id",               c.getId());                                          // ✅ FIX
        res.put("cardNo",           nvl(c.getCardNo()));
        res.put("cardType",         nvl(c.getCardType()));
        res.put("status",           nvl(c.getStatus()));
        res.put("holderName",       nvl(c.getHolderName()));
        res.put("monthlyRiceKg",    c.getMonthlyRiceKg()  != null ? c.getMonthlyRiceKg()  : 0);
        res.put("cashAmount",       c.getCashAmount()      != null ? c.getCashAmount()     : 0);
        res.put("startDate",        c.getStartDate()       != null ? c.getStartDate().toString() : "");
        res.put("endDate",          c.getEndDate()         != null ? c.getEndDate().toString()   : "");
        res.put("lastReceivedDate", c.getLastReceivedDate()!= null ? c.getLastReceivedDate().toString() : "");
        res.put("rejectionReason",  nvl(c.getRejectionReason()));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/expiring")
    public List<VgdCard> expiringSoon(@RequestParam(defaultValue = "30") int days) {
        return svc.getExpiringSoon(days);
    }

    // ── STATUS UPDATE ─────────────────────────────────────────
    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(
            @PathVariable int id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "status আবশ্যক।"));
        svc.updateStatus(id, status,
                body.getOrDefault("approvedBy", "Admin"),
                body.getOrDefault("rejectionReason", null), body.get("signatureBase64"));
        return ResponseEntity.ok(Map.of("message", "স্ট্যাটাস আপডেট হয়েছে।"));
    }

    /**
     * ✅ FIX Bug 6: distribute now inserts into vgd_distribution table.
     * Body: { distMonth: "YYYY-MM", distributedBy: "...", remarks: "..." }
     * Returns success/failure with reason (duplicate month, not approved, etc.)
     */
    @PutMapping("/distribute/{id}")
    public ResponseEntity<Object> recordDistribution(
            @PathVariable int id, @RequestBody(required = false) Map<String, String> body) {
        if (body == null) body = new HashMap<>();

        String distMonth     = body.getOrDefault("distMonth",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        String distributedBy = body.getOrDefault("distributedBy", "Admin");
        String remarks       = body.getOrDefault("remarks", null);

        Map<String, Object> result = svc.recordDistribution(id, distMonth, distributedBy, remarks);
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        return ok ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/{id}/distribution-history")
    public ResponseEntity<Object> distributionHistory(@PathVariable int id) {
        return ResponseEntity.ok(svc.getDistributionHistory(id));
    }

    // ── RENEWAL ──────────────────────────────────────────────
    @PutMapping("/renew/{id}")
    public ResponseEntity<Object> renew(@PathVariable int id) {
        VgdCard c = svc.getById(id);
        if (c == null) return ResponseEntity.notFound().build();
        if (!"Approved".equalsIgnoreCase(c.getStatus()))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "শুধুমাত্র Approved কার্ড renewal করা যাবে।"));
        svc.renew(id);
        VgdCard updated = svc.getById(id);
        return ResponseEntity.ok(Map.of(
                "message",   "কার্ড " + c.getCycleMonths() + " মাস নবায়ন হয়েছে।",
                "endDate",   updated.getEndDate() != null ? updated.getEndDate().toString() : ""
        ));
    }

    /**
     * ✅ FIX Bug 8: PDF download guard removed.
     * Any card (Approved, Suspended, Rejected) can be downloaded — admin needs it for records.
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadCard(@PathVariable int id) {
        VgdCard c = svc.getById(id);
        if (c == null) return ResponseEntity.notFound().build();

        byte[] pdf = pdfService.generateCard(c);
        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=\"" + c.getCardType() + "Card_" + c.getCardNo() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // ── DELETE ────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        if (svc.getById(id) == null) return ResponseEntity.notFound().build();
        svc.delete(id);
        return ResponseEntity.ok(Map.of("message", "মুছে ফেলা হয়েছে।"));
    }

    // ── HELPERS ───────────────────────────────────────────────
    private String saveFile(MultipartFile f, String prefix, Path dir) throws IOException {
        String name = prefix + UUID.randomUUID() + "." + ext(f.getOriginalFilename());
        Files.copy(f.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return "uploads/" + name;
    }
    private String ext(String n) { return (n!=null&&n.contains(".")) ? n.substring(n.lastIndexOf('.')+1).toLowerCase() : "bin"; }
    private BigDecimal dec(String s) { try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; } }
    private String nvl(String s) { return s != null ? s : ""; }
}
