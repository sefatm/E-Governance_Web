package com.mgt.controller;

import com.mgt.model.VgdStock;
import com.mgt.service.VgdDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/vgd")
public class VgdDistributionController {

    @Autowired VgdDistributionService svc;

    // ── STOCK ─────────────────────────────────────────────────
    @PostMapping("/stock")
    public ResponseEntity<Object> createStock(@RequestBody Map<String, Object> b) {
        if (b.get("batchLabel")==null || b.get("ward")==null ||
            b.get("cycleMonth")==null || b.get("totalCards")==null)
            return ResponseEntity.badRequest()
                    .body(Map.of("message","batchLabel, ward, cycleMonth, totalCards আবশ্যক।"));

        VgdStock s = new VgdStock();
        s.setBatchLabel(str(b,"batchLabel"));
        s.setCycleMonth(str(b,"cycleMonth"));
        s.setCardType(b.getOrDefault("cardType","VGD").toString().toUpperCase());
        s.setWard(str(b,"ward"));
        s.setDistrict(str(b,"district"));
        s.setDealerName(str(b,"dealerName"));
        s.setRiceKg(dec(b,"riceKg"));
        s.setWheatKg(dec(b,"wheatKg"));
        s.setCashAmount(dec(b,"cashAmount"));
        s.setTotalCards(num(b,"totalCards"));
        s.setDistributed(0);
        return ResponseEntity.ok(svc.createStock(s));
    }

    @GetMapping("/stock")
    public ResponseEntity<Object> getAllStock() {
        return ResponseEntity.ok(svc.getAllStock());
    }

    // ── SESSION ───────────────────────────────────────────────
    @PostMapping("/session/open")
    public ResponseEntity<Object> openSession(@RequestBody Map<String, String> b) {
        String ward  = b.get("ward");
        String cycle = b.get("cycleMonth");
        String type  = b.getOrDefault("cardType","VGD");
        if (ward==null||ward.isBlank()||cycle==null||cycle.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("error","ward ও cycleMonth আবশ্যক।"));
        Map<String,Object> res = svc.openSession(
                b.getOrDefault("dealerName","Dealer"), ward, cycle, type);
        return res.containsKey("error")
                ? ResponseEntity.badRequest().body(res)
                : ResponseEntity.ok(res);
    }

    @PutMapping("/session/{id}/close")
    public ResponseEntity<Object> closeSession(@PathVariable int id) {
        Map<String,Object> res = svc.closeSession(id);
        return res.containsKey("error")
                ? ResponseEntity.badRequest().body(res)
                : ResponseEntity.ok(res);
    }

    @GetMapping("/sessions")
    public ResponseEntity<Object> allSessions() {
        return ResponseEntity.ok(svc.getAllSessions());
    }

    @GetMapping("/session/{id}")
    public ResponseEntity<Object> sessionDetail(@PathVariable int id) {
        Map<String,Object> res = svc.getSessionDetail(id);
        return res.containsKey("error")
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(res);
    }

    // ── SCAN ──────────────────────────────────────────────────
    /**
     * POST /api/vgd/scan
     * Body: { sessionId, cardNo, scannedBy }
     * QR format: "CARD:VGD-02-2025-00042" — service strips prefix
     */
    @PostMapping("/scan")
    public ResponseEntity<Object> scan(@RequestBody Map<String, Object> b) {
        int    sid    = num(b, "sessionId");
        String cardNo = str(b, "cardNo");
        String by     = b.getOrDefault("scannedBy","dealer").toString();
        if (sid<=0 || cardNo==null || cardNo.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("success",false,"message","sessionId ও cardNo আবশ্যক।"));
        cardNo = parseCardNo(cardNo);
        if (cardNo.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "অবৈধ Social Card QR কোড।"));
        }
        Map<String,Object> res = svc.scan(sid, cardNo, by);
        return Boolean.TRUE.equals(res.get("success"))
                ? ResponseEntity.ok(res)
                : ResponseEntity.badRequest().body(res);
    }

    // ── HISTORY ───────────────────────────────────────────────
    @GetMapping("/history/card/{cardId}")
    public ResponseEntity<Object> historyByCardId(@PathVariable int cardId) {
        return ResponseEntity.ok(svc.getCardHistory(cardId));
    }

    @GetMapping("/history/cardno/{cardNo}")
    public ResponseEntity<Object> historyByCardNo(@PathVariable String cardNo) {
        return ResponseEntity.ok(svc.getCardHistoryByCardNo(cardNo));
    }

    @GetMapping("/cycle-summary/{cycleMonth}")
    public ResponseEntity<Object> cycleSummary(
            @PathVariable String cycleMonth,
            @RequestParam(required=false) String cardType) {
        return ResponseEntity.ok(svc.getCycleSummary(cycleMonth, cardType));
    }

    private String parseCardNo(String raw) {
        if (raw == null) return "";
        String value = raw.trim();
        if (value.startsWith("EGOV_CARD|")) {
            for (String part : value.split("\\|")) {
                int eq = part.indexOf('=');
                if (eq > 0 && "CARD_NO".equalsIgnoreCase(part.substring(0, eq).trim())) {
                    return part.substring(eq + 1).trim();
                }
            }
            return "";
        }
        String[] prefixes = { "VGD_CARD:", "CARD:" };
        for (String prefix : prefixes) {
            if (value.startsWith(prefix)) {
                value = value.substring(prefix.length());
                int pipe = value.indexOf('|');
                return (pipe >= 0 ? value.substring(0, pipe) : value).trim();
            }
        }
        return value;
    }

    // ── HELPERS ───────────────────────────────────────────────
    private String     str(Map<String,Object> m,String k){ Object v=m.get(k); return v!=null?v.toString().trim():null; }
    private int        num(Map<String,Object> m,String k){ Object v=m.get(k); if(v==null)return 0; try{return Integer.parseInt(v.toString());}catch(Exception e){return 0;} }
    private BigDecimal dec(Map<String,Object> m,String k){ Object v=m.get(k); if(v==null)return BigDecimal.ZERO; try{return new BigDecimal(v.toString());}catch(Exception e){return BigDecimal.ZERO;} }
}
