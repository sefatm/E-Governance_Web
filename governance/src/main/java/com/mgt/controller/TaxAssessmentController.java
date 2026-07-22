package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.TaxAssessment;
import com.mgt.service.TaxAssessmentService;

@RestController
@RequestMapping("/api/tax-assessment")
public class TaxAssessmentController {

    @Autowired
    private TaxAssessmentService service;

    // POST /api/tax-assessment/create
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody TaxAssessment assessment) {
        try {
            TaxAssessment saved = service.create(assessment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // GET /api/tax-assessment/getall
    @GetMapping("/getall")
    public ResponseEntity<List<TaxAssessment>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET /api/tax-assessment/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        TaxAssessment a = service.getById(id);
        if (a == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(a);
    }

    // GET /api/tax-assessment/holding/{holdingNo}
    @GetMapping("/holding/{holdingNo}")
    public ResponseEntity<List<TaxAssessment>> getByHoldingNo(@PathVariable String holdingNo) {
        return ResponseEntity.ok(service.getByHoldingNo(holdingNo));
    }

    // PUT /api/tax-assessment/status/{id}
    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody String status) {
        service.updateStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "Status updated to " + status));
    }

    // DELETE /api/tax-assessment/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Assessment deleted"));
    }
}
