package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.TaxPayment;
import com.mgt.service.TaxAssessmentService;
import com.mgt.service.TaxPaymentService;

@RestController
@RequestMapping("/api/tax-payment")
public class TaxPaymentController {

    @Autowired
    private TaxPaymentService service;

    @Autowired
    private TaxAssessmentService assessmentService;

    // POST /api/tax-payment/create
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody TaxPayment payment) {
        try {
            TaxPayment saved = service.create(payment);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // GET /api/tax-payment/getall
    @GetMapping("/getall")
    public ResponseEntity<List<TaxPayment>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET /api/tax-payment/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        TaxPayment p = service.getById(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    // GET /api/tax-payment/holding/{holdingNo}
    @GetMapping("/holding/{holdingNo}")
    public ResponseEntity<List<TaxPayment>> getByHoldingNo(@PathVariable String holdingNo) {
        return ResponseEntity.ok(service.getByHoldingNo(holdingNo));
    }

    // GET /api/tax-payment/due/{holdingNo}
    @GetMapping("/due/{holdingNo}")
    public ResponseEntity<Object> getDue(@PathVariable String holdingNo) {
        var assessments = assessmentService.getByHoldingNo(holdingNo);
        if (assessments.isEmpty()) return ResponseEntity.notFound().build();

        double totalAssessed = assessments.stream()
            .mapToDouble(a -> a.getTotalPayable() != null ? a.getTotalPayable() : 0.0)
            .sum();
        double totalPaid = service.getTotalPaidByHoldingNo(holdingNo);
        double due       = totalAssessed - totalPaid;

        return ResponseEntity.ok(Map.of(
            "holdingNo",      holdingNo,
            "ownerName",      assessments.get(0).getOwnerName(),
            "totalAssessed",  totalAssessed,
            "totalPaid",      totalPaid,
            "due",            due,
            "status",         due <= 0 ? "Paid" : "Due"
        ));
    }

    // GET /api/tax-payment/due-list
    @GetMapping("/due-list")
    public ResponseEntity<Object> getAllDue() {
        var assessments = assessmentService.getAll();

        var dueList = assessments.stream().map(a -> {
            double paid = service.getTotalPaidByHoldingNo(a.getHoldingNo());
            double total = a.getTotalPayable() != null ? a.getTotalPayable() : 0.0;
            double due   = total - paid;
            return Map.of(
                "holdingNo",    a.getHoldingNo(),
                "ownerName",    a.getOwnerName(),
                "propertyType", a.getPropertyType() != null ? a.getPropertyType() : "",
                "totalPayable", total,
                "paid",         paid,
                "due",          due,
                "status",       due <= 0 ? "Paid" : "Due"
            );
        }).toList();

        return ResponseEntity.ok(dueList);
    }

    // PUT /api/tax-payment/status/{id}
    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody String status) {
        service.updateStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "Status updated to " + status));
    }

    // DELETE /api/tax-payment/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Payment record deleted"));
    }
}
