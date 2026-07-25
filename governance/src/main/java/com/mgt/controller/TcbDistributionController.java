package com.mgt.controller;

import com.mgt.model.TcbStock;
import com.mgt.service.TcbDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/tcb")
public class TcbDistributionController {

    @Autowired
    TcbDistributionService svc;

    // ── STOCK ─────────────────────────────────────────────────────────────────

    @PostMapping("/stock")
    public ResponseEntity<Object> createStock(@RequestBody Map<String, Object> body) {
        if (body.get("batchLabel") == null || body.get("ward") == null
                || body.get("cycleMonth") == null || body.get("totalCards") == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "batchLabel, ward, cycleMonth, totalCards আবশ্যক।"));
        }

        TcbStock s = new TcbStock();
        s.setBatchLabel((String) body.get("batchLabel"));
        s.setCycleMonth((String) body.get("cycleMonth"));
        s.setWard((String) body.get("ward"));
        s.setDealerName((String) body.getOrDefault("dealerName", ""));
        s.setOilLitre(dec(body, "oilLitre"));
        s.setRiceKg(dec(body, "riceKg"));
        s.setLentilKg(dec(body, "lentilKg"));
        s.setSugarKg(dec(body, "sugarKg"));
        s.setCashAmount(dec(body, "cashAmount"));
        s.setOilPricePerLitre(dec(body, "oilPricePerLitre"));
        s.setRicePricePerKg(dec(body, "ricePricePerKg"));
        s.setLentilPricePerKg(dec(body, "lentilPricePerKg"));
        s.setSugarPricePerKg(dec(body, "sugarPricePerKg"));
        s.setTotalCards(num(body, "totalCards"));
        s.setDistributed(0); 

        return ResponseEntity.ok(svc.createStock(s));
    }

    @GetMapping("/stock")
    public ResponseEntity<Object> getAllStock() {
        return ResponseEntity.ok(svc.getAllStock());
    }

    // ── SESSION ───────────────────────────────────────────────────────────────

    @PostMapping("/session/open")
    public ResponseEntity<Object> openSession(@RequestBody Map<String, String> body) {

        String dealerName = body.get("dealerName");
        String ward = body.get("ward");
        String cycleMonth = body.get("cycleMonth");
        String distributionDate = body.get("distributionDate");
        String distributionTime = body.get("distributionTime");
        String location = body.get("location");

        if (ward == null || ward.isBlank()
                || cycleMonth == null || cycleMonth.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "ward এবং cycleMonth আবশ্যক।"));
        }

        Map<String, Object> result =
                svc.openSession(dealerName, ward, cycleMonth, distributionDate, distributionTime, location);

        if (result.containsKey("error"))
            return ResponseEntity.badRequest().body(result);

        return ResponseEntity.ok(result);
    }

    @PutMapping("/session/{id}/close")
    public ResponseEntity<Object> closeSession(@PathVariable int id) {
        return ResponseEntity.ok(svc.closeSession(id));
    }

    @GetMapping("/session/{id}/status")
    public ResponseEntity<Object> sessionStatus(@PathVariable int id) {
        Map<String, Object> result = svc.getSessionStatus(id);
        if (result.containsKey("error"))
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/sessions")
    public ResponseEntity<Object> allSessions() {
        return ResponseEntity.ok(svc.getAllSessions());
    }

    // ── SCAN ──────────────────────────────────────────────────────────────────
    @PostMapping("/scan")
    public ResponseEntity<Object> scan(@RequestBody Map<String, Object> body) {

        System.out.println("REQUEST = " + body);

        int sessionId = num(body, "sessionId");
        String cardNo = (String) body.get("cardNo");

        System.out.println("SESSION ID = " + sessionId);
        System.out.println("CARD NO = " + cardNo);

        Map<String, Object> result =
                svc.scan(sessionId, cardNo.trim(), "dealer");

        System.out.println("RESULT = " + result);

        boolean ok = Boolean.TRUE.equals(result.get("success"));

        return ok
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }

    // ── HISTORY ───────────────────────────────────────────────────────────────
    @GetMapping("/history/{cardNo}")
    public ResponseEntity<Object> history(@PathVariable String cardNo) {
        if (cardNo == null || cardNo.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "cardNo আবশ্যক।"));
        return ResponseEntity.ok(svc.getHistoryByCard(cardNo));
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private BigDecimal dec(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return BigDecimal.ZERO;
        try { return new BigDecimal(v.toString()); }
        catch (Exception e) { return BigDecimal.ZERO; }
    }

    private int num(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return 0;
        try { return Integer.parseInt(v.toString()); }
        catch (Exception e) { return 0; }
    }
}
