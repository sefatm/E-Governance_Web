package com.mgt.controller;

import com.mgt.model.LpgCard;
import com.mgt.service.LpgCardService;
import com.mgt.service.LpgCardPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lpg-card")
public class LpgCardController {

    @Autowired LpgCardService    lpgCardService;
    @Autowired LpgCardPdfService pdfService;

    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    // ══════════════════════════════════════════════════════════
    // CARD MANAGEMENT (আগের মতো)
    // ══════════════════════════════════════════════════════════

    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> apply(
            @RequestParam("holderName")                               String holderName,
            @RequestParam("nid")                                      String nid,
            @RequestParam(value = "dateOfBirth",    required = false) String dateOfBirth,
            @RequestParam("contact")                                  String contact,
            @RequestParam("address")                                  String address,
            @RequestParam(value = "ward",           required = false) String ward,
            @RequestParam(value = "unionName",      required = false) String unionName,
            @RequestParam(value = "upazila",        required = false) String upazila,
            @RequestParam(value = "district",       required = false) String district,
            @RequestParam(value = "membersCount",   required = false, defaultValue = "1") int membersCount,
            @RequestParam(value = "hasGasLine",     required = false, defaultValue = "false") String hasGasLine,
            @RequestParam(value = "stoveCount",     required = false, defaultValue = "1") int stoveCount,
            @RequestParam(value = "dealerName",     required = false) String dealerName,
            @RequestParam(value = "dealerCode",     required = false) String dealerCode,
            @RequestParam(value = "dealerContact",  required = false) String dealerContact,
            @RequestParam(value = "cylinderSize",   required = false, defaultValue = "12kg") String cylinderSize,
            @RequestParam(value = "photo",          required = false) MultipartFile photo,
            @RequestParam(value = "nidFile",        required = false) MultipartFile nidFile
    ) {
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
        try { Files.createDirectories(uploadPath); } catch (Exception ignored) {}

        String photoUrl = null, nidFileUrl = null;
        try {
            if (photo   != null && !photo.isEmpty())   photoUrl   = saveFile(photo,   "lpg_photo_", uploadPath);
            if (nidFile != null && !nidFile.isEmpty()) nidFileUrl = saveFile(nidFile, "lpg_nid_",   uploadPath);
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "File upload failed: " + e.getMessage()));
        }

        LpgCard card = new LpgCard();
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
        card.setHasGasLine("true".equalsIgnoreCase(hasGasLine));
        card.setStoveCount(stoveCount);
        card.setDealerName(dealerName);
        card.setDealerCode(dealerCode);
        card.setDealerContact(dealerContact);
        card.setCylinderSize(cylinderSize);
        card.setPhotoUrl(photoUrl);
        card.setNidFileUrl(nidFileUrl);
        card.setCreatedAt(LocalDateTime.now());

        String result = lpgCardService.create(card);
        if ("HAS_GAS_LINE".equals(result))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "পাইপলাইন গ্যাস সংযোগ থাকলে LPG কার্ডের জন্য যোগ্য নন।"));
        if ("DUPLICATE".equals(result))
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "এই NID দিয়ে আগেই আবেদন করা হয়েছে।"));

        return ResponseEntity.ok(Map.of(
                "message", "এলপিজি কার্ডের আবেদন সফলভাবে জমা হয়েছে।",
                "cardNo",  card.getCardNo()
        ));
    }

    @GetMapping("/getall")
    public ResponseEntity<Object> getAll() { return ResponseEntity.ok(lpgCardService.getAll()); }

    @GetMapping("/status/{status}")
    public ResponseEntity<Object> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(lpgCardService.getByStatus(status));
    }

    @GetMapping("/dealer/{dealerCode}")
    public ResponseEntity<Object> getByDealer(@PathVariable String dealerCode) {
        return ResponseEntity.ok(lpgCardService.getByDealer(dealerCode));
    }

    @GetMapping("/district/{district}")
    public ResponseEntity<Object> getByDistrict(@PathVariable String district) {
        return ResponseEntity.ok(lpgCardService.getByDistrict(district));
    }


    @GetMapping("/by-cardno/{cardNo}")
    public ResponseEntity<Object> getByCardNo(@PathVariable String cardNo) {
        LpgCard card = lpgCardService.getByCardNo(cardNo);
        if (card == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(card);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        LpgCard card = lpgCardService.getById(id);
        if (card == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(card);
    }

    @GetMapping("/check/{nid}")
    public ResponseEntity<Object> checkByNid(@PathVariable String nid) {
        LpgCard card = lpgCardService.getByNid(nid);
        if (card == null) return ResponseEntity.notFound().build();
        Map<String, Object> res = new java.util.LinkedHashMap<>();
        res.put("id",              card.getId());
        res.put("cardNo",          card.getCardNo() != null ? card.getCardNo() : "");
        res.put("status",          card.getStatus() != null ? card.getStatus() : "");
        res.put("holderName",      card.getHolderName() != null ? card.getHolderName() : "");
        res.put("monthlyQuota",    card.getMonthlyQuota());
        res.put("cylinderSize",    card.getCylinderSize() != null ? card.getCylinderSize() : "");
        res.put("dealerName",      card.getDealerName() != null ? card.getDealerName() : "");
        res.put("lastCollectedAt", card.getLastCollectedAt() != null ? card.getLastCollectedAt().toString() : "");
        res.put("rejectionReason", card.getRejectionReason() != null ? card.getRejectionReason() : "");
        return ResponseEntity.ok(res);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id,
                                               @RequestBody Map<String, String> body) {
        lpgCardService.updateStatus(id,
                body.get("status"),
                body.getOrDefault("approvedBy", "Admin"),
                body.getOrDefault("rejectionReason", null), body.get("signatureBase64"));
        return ResponseEntity.ok(Map.of("message", "স্ট্যাটাস আপডেট হয়েছে।"));
    }

    /**
     * PUT /api/lpg-card/collect/{id}
     * Simple "mark collected" — আগের endpoint অপরিবর্তিত।
     * নতুন distribute endpoint-এর সাথে পার্থক্য:
     *   /collect → শুধু lastCollectedAt update করে (পুরনো flow)
     *   /distribute → পূর্ণ log + stock deduct (নতুন flow)
     */
    @PutMapping("/collect/{id}")
    public ResponseEntity<Object> recordCollection(@PathVariable int id) {
        LpgCard card = lpgCardService.getById(id);
        if (card == null) return ResponseEntity.notFound().build();
        if (!"Approved".equalsIgnoreCase(card.getStatus()))
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "কার্ডটি অনুমোদিত নয়।"));
        // delegate to service
        String cycleMonth = java.time.YearMonth.now().toString();
        Map<String, Object> result = lpgCardService.recordDistribution(id, cycleMonth, 1, "Dealer");
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        return ok ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadCard(@PathVariable int id) {
        LpgCard card = lpgCardService.getById(id);
        if (card == null) return ResponseEntity.notFound().build();
        byte[] pdf = pdfService.generateCard(card);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"LPGCard_" + card.getCardNo() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        lpgCardService.delete(id);
        return ResponseEntity.ok(Map.of("message", "মুছে ফেলা হয়েছে।"));
    }

    // ══════════════════════════════════════════════════════════
    // DISTRIBUTION — সিলিন্ডার বিতরণ রেকর্ড (নতুন)
    // ══════════════════════════════════════════════════════════

    /**
     * POST /api/lpg-card/distribute
     * Dealer QR scan করে বিতরণ রেকর্ড করে।
     * Body: { cardId, cycleMonth, cylindersQty?, collectedBy? }
     */
    @PostMapping("/distribute")
    public ResponseEntity<Object> distribute(@RequestBody Map<String, Object> body) {
        String cycleMonth  = str(body, "cycleMonth");
        int    qty         = body.containsKey("cylindersQty") ? num(body, "cylindersQty") : 1;
        String collectedBy = body.getOrDefault("collectedBy", "Dealer").toString();
 
        // cardId অথবা cardNo — যেকোনো একটা থেকে card খুঁজব
        int cardId = num(body, "cardId");
 
        if (cardId <= 0) {
            // cardNo দিয়ে lookup
            String cardNo = str(body, "cardNo");
            if (cardNo == null || cardNo.isBlank())
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "cardId অথবা cardNo আবশ্যক।"));
 
            LpgCard card = lpgCardService.getByCardNo(cardNo);
            if (card == null)
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "কার্ড নম্বর পাওয়া যায়নি: " + cardNo));
            cardId = card.getId();
        }
 
        if (cycleMonth == null || cycleMonth.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "cycleMonth আবশ্যক।"));
 
        Map<String, Object> result = lpgCardService.recordDistribution(cardId, cycleMonth, qty, collectedBy);
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        return ok ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    // ══════════════════════════════════════════════════════════
    // HISTORY — বিতরণ ইতিহাস (নতুন)
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/lpg-card/history/{cardId}
     * একটি কার্ডের সম্পূর্ণ ইতিহাস।
     */
    @GetMapping("/history/{cardId}")
    public ResponseEntity<Object> historyByCard(@PathVariable int cardId) {
        return ResponseEntity.ok(lpgCardService.getHistoryByCardId(cardId));
    }

    /**
     * GET /api/lpg-card/history/by-cardno/{cardNo}
     * কার্ড নম্বর দিয়ে ইতিহাস (Admin lookup)।
     */
    @GetMapping("/history/by-cardno/{cardNo}")
    public ResponseEntity<Object> historyByCardNo(@PathVariable String cardNo) {
        return ResponseEntity.ok(lpgCardService.getHistoryByCardNo(cardNo));
    }

    /**
     * GET /api/lpg-card/cycle-summary/{cycleMonth}
     * একটি চক্রের সব বিতরণ সারসংক্ষেপ।
     */
    @GetMapping("/cycle-summary/{cycleMonth}")
    public ResponseEntity<Object> cycleSummary(@PathVariable String cycleMonth) {
        return ResponseEntity.ok(lpgCardService.getCycleSummary(cycleMonth));
    }

    /**
     * GET /api/lpg-card/dealer-history?cycleMonth=YYYY-MM&dealerCode=DLR001
     * ডিলারের নিজের বিতরণ ইতিহাস।
     */
    @GetMapping("/dealer-history")
    public ResponseEntity<Object> dealerHistory(
            @RequestParam String cycleMonth,
            @RequestParam String dealerCode) {
        return ResponseEntity.ok(lpgCardService.getDealerHistory(cycleMonth, dealerCode));
    }

    // ══════════════════════════════════════════════════════════
    // STOCK — সিলিন্ডার মজুদ ব্যবস্থাপনা (নতুন)
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/lpg-card/stock?cycleMonth=YYYY-MM
     * History page-এ স্টক সারসংক্ষেপ লোড।
     */
    @GetMapping("/stock")
    public ResponseEntity<Object> getStock(
            @RequestParam(required = false) String cycleMonth) {
        return ResponseEntity.ok(lpgCardService.getStockList(cycleMonth));
    }

    /**
     * POST /api/lpg-card/stock
     * Admin নতুন সিলিন্ডার স্টক এন্ট্রি।
     * Body: { cycleMonth, batchLabel?, ward?, dealerName?, dealerCode?,
     *         cylinderSize?, totalCylinders, totalCards? }
     */
    @PostMapping("/stock")
    public ResponseEntity<Object> saveStock(@RequestBody Map<String, Object> body) {
        String cycleMonth      = str(body, "cycleMonth");
        String batchLabel      = str(body, "batchLabel");
        String ward            = str(body, "ward");
        String dealerName      = str(body, "dealerName");
        String dealerCode      = str(body, "dealerCode");
        String cylinderSize    = str(body, "cylinderSize");
        int    totalCylinders  = num(body, "totalCylinders");
        int    totalCards      = body.containsKey("totalCards") ? num(body, "totalCards") : 0;

        if (cycleMonth == null || cycleMonth.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "cycleMonth আবশ্যক।"));
        if (totalCylinders <= 0)
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "totalCylinders ০-এর বেশি হতে হবে।"));

        Map<String, Object> result = lpgCardService.saveStock(
                cycleMonth, batchLabel, ward, dealerName, dealerCode,
                cylinderSize, totalCylinders, totalCards);
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        return ok ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    // ── HELPERS ───────────────────────────────────────────────
    private String str(Map<String, Object> m, String k) {
        Object v = m.get(k); return v != null ? v.toString().trim() : null;
    }
    private int num(Map<String, Object> m, String k) {
        Object v = m.get(k); if (v == null) return 0;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }

    private String saveFile(MultipartFile file, String prefix, Path dir) throws IOException {
        String filename = prefix + UUID.randomUUID() + "." + getExt(file.getOriginalFilename());
        Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return "uploads/" + filename;
    }
    private String getExt(String name) {
        if (name == null || !name.contains(".")) return "bin";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}
