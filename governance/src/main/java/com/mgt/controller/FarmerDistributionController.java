package com.mgt.controller;

import com.mgt.service.FarmerDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/farmer")
public class FarmerDistributionController {

    @Autowired FarmerDistributionService svc;

    // ══════════════════════════════════════════════════════════
    // PHYSICAL DISTRIBUTION — সার / বীজ
    // ══════════════════════════════════════════════════════════

    /**
     * POST /api/farmer/distribute
     * Record fertilizer/seed distribution after QR scan.
     * Body: { cardId, cycleMonth, season, fertilizerKg, seedKg,
     *         pesticideLitre, distributedBy, sessionId }
     */
    @PostMapping("/distribute")
    public ResponseEntity<Object> distribute(@RequestBody Map<String, Object> body) {
        int     cardId       = num(body, "cardId");
        String  cycleMonth   = str(body, "cycleMonth");
        String  season       = str(body, "season");
        BigDecimal fertKg    = dec(body, "fertilizerKg");
        BigDecimal seedKg    = dec(body, "seedKg");
        BigDecimal pestLitre = dec(body, "pesticideLitre");
        String  distributedBy= body.getOrDefault("distributedBy", "System").toString();
        Integer sessionId    = body.containsKey("sessionId") ? num(body,"sessionId") : null;

        if (cardId <= 0 || cycleMonth == null || cycleMonth.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("success",false,"message","cardId ও cycleMonth আবশ্যক।"));

        Map<String, Object> result = svc.distribute(
                cardId, cycleMonth, season, fertKg, seedKg, pestLitre, distributedBy, sessionId
        );
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        return ok ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    /**
     * GET /api/farmer/subsidy-history/{cardId}
     */
    @GetMapping("/subsidy-history/{cardId}")
    public ResponseEntity<Object> subsidyHistory(@PathVariable int cardId) {
        return ResponseEntity.ok(svc.getSubsidyHistory(cardId));
    }

    /**
     * GET /api/farmer/cycle-summary/by-card/{cardNo}
     * History by card number string (Angular service uses this).
     */
    @GetMapping("/cycle-summary/by-card/{cardNo}")
    public ResponseEntity<Object> subsidyHistoryByCardNo(@PathVariable String cardNo) {
        return ResponseEntity.ok(svc.getSubsidyHistoryByCardNo(cardNo));
    }

    /**
     * GET /api/farmer/cycle-summary/{cycleMonth}
     */
    @GetMapping("/cycle-summary/{cycleMonth}")
    public ResponseEntity<Object> cycleSummary(@PathVariable String cycleMonth) {
        return ResponseEntity.ok(svc.getCycleSummary(cycleMonth));
    }

    // ══════════════════════════════════════════════════════════
    // STOCK MANAGEMENT — সার/বীজ মজুদ ব্যবস্থাপনা (নতুন)
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/farmer/stock?cycleMonth=YYYY-MM
     * History page-এ স্টক সারসংক্ষেপ লোড করে।
     * cycleMonth param ছাড়া দিলে সব স্টক দেবে।
     */
    @GetMapping("/stock")
    public ResponseEntity<Object> getStock(
            @RequestParam(required = false) String cycleMonth) {
        return ResponseEntity.ok(svc.getStockList(cycleMonth));
    }

    /**
     * POST /api/farmer/stock
     * Admin নতুন স্টক এন্ট্রি যোগ করেন।
     * Body: { cycleMonth, batchNo?, fertilizerKg, seedKg, pesticideLitre?, note? }
     */
    @PostMapping("/stock")
    public ResponseEntity<Object> saveStock(@RequestBody Map<String, Object> body) {
        String     cycleMonth    = str(body, "cycleMonth");
        String     batchNo       = str(body, "batchNo");
        BigDecimal fertilizerKg  = dec(body, "fertilizerKg");
        BigDecimal seedKg        = dec(body, "seedKg");
        BigDecimal pesticideLitre= dec(body, "pesticideLitre");
        String     note          = str(body, "note");

        if (cycleMonth == null || cycleMonth.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "cycleMonth আবশ্যক।"));
        if (fertilizerKg.compareTo(BigDecimal.ZERO) < 0 || seedKg.compareTo(BigDecimal.ZERO) < 0)
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "পরিমাণ ঋণাত্মক হতে পারবে না।"));

        Map<String, Object> result = svc.saveStock(
                cycleMonth, batchNo, fertilizerKg, seedKg, pesticideLitre, note);
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        return ok ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    // ══════════════════════════════════════════════════════════
    // G2P BANK TRANSFER — নগদ ভর্তুকি
    // ══════════════════════════════════════════════════════════

    @GetMapping("/g2p/batches")
    public ResponseEntity<Object> allBatches() {
        return ResponseEntity.ok(svc.getAllBatches());
    }

    @PostMapping("/g2p/batch")
    public ResponseEntity<Object> createBatch(@RequestBody Map<String, Object> body) {
        String  cycleMonth = str(body, "cycleMonth");
        String  ward       = str(body, "ward");
        String  district   = str(body, "district");
        BigDecimal amount  = dec(body, "amountPerFarmer");
        String  gateway    = body.getOrDefault("gateway", "BEFTN").toString();
        String  by         = body.getOrDefault("submittedBy", "Admin").toString();

        if (cycleMonth == null || cycleMonth.isBlank() || amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
            return ResponseEntity.badRequest()
                    .body(Map.of("success",false,"message","cycleMonth ও amountPerFarmer আবশ্যক।"));

        Map<String, Object> result = svc.createG2pBatch(
                cycleMonth,
                (ward     != null && ward.isBlank()     ? null : ward),
                (district != null && district.isBlank() ? null : district),
                amount, gateway, by
        );
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        return ok ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @PutMapping("/g2p/batch/{id}/submit")
    public ResponseEntity<Object> submitBatch(
            @PathVariable int id, @RequestBody Map<String, String> body) {
        Map<String, Object> result = svc.submitBatch(id, body.getOrDefault("submittedBy", "Admin"));
        boolean ok = Boolean.TRUE.equals(result.get("success"));
        return ok ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

    @GetMapping("/g2p/batch/{id}")
    public ResponseEntity<Object> batchDetail(@PathVariable int id) {
        return ResponseEntity.ok(svc.getBatchSummary(id));
    }

    @GetMapping("/g2p/batch/{id}/transfers")
    public ResponseEntity<Object> batchTransfers(@PathVariable int id) {
        return ResponseEntity.ok(svc.getTransfersByBatch(id));
    }

    @PutMapping("/g2p/batch/{id}/retry")
    public ResponseEntity<Object> retryFailed(@PathVariable int id) {
        return ResponseEntity.ok(svc.retryFailed(id));
    }

    @PostMapping("/g2p/callback")
    public ResponseEntity<Object> webhook(@RequestBody Map<String, Object> body) {
        String txnRef        = str(body, "txnRef");
        String providerTxnId = str(body, "providerTxnId");
        String status        = str(body, "status");
        String failureReason = str(body, "failureReason");

        if (txnRef == null || txnRef.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error","txnRef আবশ্যক।"));

        Map<String, Object> result = svc.handleCallback(txnRef, providerTxnId, status, failureReason);
        return ResponseEntity.ok(result);
    }

    // ── HELPERS ───────────────────────────────────────────────
    private String str(Map<String, Object> m, String k) {
        Object v = m.get(k); return v != null ? v.toString().trim() : null;
    }
    private int num(Map<String, Object> m, String k) {
        Object v = m.get(k); if (v==null) return 0;
        try { return Integer.parseInt(v.toString()); } catch (Exception e) { return 0; }
    }
    private BigDecimal dec(Map<String, Object> m, String k) {
        Object v = m.get(k); if (v==null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }
}
