package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.TradeInspection;
import com.mgt.service.TradeInspectionService;

/**
 * TradeInspectionController — Physical Inspection Scheduling
 *
 * POST   /api/trade-inspection/schedule          → Inspection schedule করো
 * GET    /api/trade-inspection/getall            → সব inspections
 * GET    /api/trade-inspection/{id}              → Single inspection
 * GET    /api/trade-inspection/license/{id}      → Specific license এর inspections
 * GET    /api/trade-inspection/status/{status}   → Status দিয়ে filter
 * GET    /api/trade-inspection/today             → আজকের inspections
 * PUT    /api/trade-inspection/complete/{id}     → Inspection result submit করো
 * PUT    /api/trade-inspection/cancel/{id}       → Cancel করো
 */
@RestController
@RequestMapping("/api/trade-inspection")
public class TradeInspectionController {

    @Autowired
    private TradeInspectionService service;

    // ─── Schedule Inspection ─────────────────────────────────────────────────
    /**
     * Admin inspection schedule করবে
     *
     * Request body:
     * {
     *   "licenseId": 5,
     *   "inspectorName": "মোঃ রফিকুল ইসলাম",
     *   "inspectorDesignation": "Assistant Engineer",
     *   "inspectionDate": "2026-06-15",
     *   "inspectionTime": "10:00:00"
     * }
     */
    @PostMapping("/schedule")
    public ResponseEntity<Object> schedule(@RequestBody TradeInspection inspection) {
        try {
            if (inspection.getLicenseId() <= 0)
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "licenseId দিতে হবে।"));
            if (inspection.getInspectionDate() == null)
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "inspectionDate দিতে হবে।"));

            TradeInspection saved = service.scheduleInspection(inspection);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Get All ─────────────────────────────────────────────────────────────
    @GetMapping("/getall")
    public ResponseEntity<List<TradeInspection>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // ─── Get by ID ───────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        TradeInspection ins = service.getById(id);
        if (ins == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(ins);
    }

    // ─── Get by License ──────────────────────────────────────────────────────
    @GetMapping("/license/{licenseId}")
    public ResponseEntity<List<TradeInspection>> getByLicense(@PathVariable int licenseId) {
        return ResponseEntity.ok(service.getByLicenseId(licenseId));
    }

    // ─── Get by Status ────────────────────────────────────────────────────────
    /** status = Scheduled / Completed / Cancelled */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TradeInspection>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    // ─── Today's Inspections ──────────────────────────────────────────────────
    @GetMapping("/today")
    public ResponseEntity<List<TradeInspection>> getToday() {
        return ResponseEntity.ok(service.getTodaysInspections());
    }

    // ─── Complete Inspection ──────────────────────────────────────────────────
    /**
     * Inspector inspection শেষে result submit করবে
     * → License automatically Approved বা Rejected হবে
     * → Applicant কে email notification যাবে
     *
     * Request body:
     * {
     *   "outcome": "Passed",                         ← "Passed" অথবা "Failed"
     *   "remarks": "সব ঠিকঠাক আছে। ব্যবসার পরিবেশ ভালো।"
     * }
     */
    @PutMapping("/complete/{id}")
    public ResponseEntity<Object> complete(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        try {
            String outcome = body.get("outcome");
            String remarks = body.get("remarks");

            if (outcome == null || (!outcome.equalsIgnoreCase("Passed") && !outcome.equalsIgnoreCase("Failed")))
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "outcome অবশ্যই 'Passed' অথবা 'Failed' হতে হবে।"));

            TradeInspection updated = service.completeInspection(id, outcome, remarks);
            String msg = "Passed".equalsIgnoreCase(outcome)
                ? "Inspection Passed। License Approved এবং Applicant কে email পাঠানো হয়েছে।"
                : "Inspection Failed। License Rejected এবং Applicant কে email পাঠানো হয়েছে।";
            return ResponseEntity.ok(Map.of("message", msg, "inspection", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─── Cancel Inspection ────────────────────────────────────────────────────
    /**
     * Request body: { "reason": "Inspector অসুস্থ। Reschedule করতে হবে।" }
     */
    @PutMapping("/cancel/{id}")
    public ResponseEntity<Object> cancel(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        try {
            TradeInspection updated = service.cancelInspection(id, body.get("reason"));
            return ResponseEntity.ok(Map.of(
                "message", "Inspection cancel করা হয়েছে।",
                "inspection", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
