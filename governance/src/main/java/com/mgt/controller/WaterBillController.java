package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.PaymentReceipt;
import com.mgt.model.PaymentTransaction;
import com.mgt.model.WaterBill;
import com.mgt.service.PaymentReceiptPdfService;
import com.mgt.service.PaymentService;
import com.mgt.service.WaterBillService;

@RestController
@RequestMapping("/api/water-bill")
public class WaterBillController {

    @Autowired WaterBillService billService;
    @Autowired PaymentService paymentService;
    @Autowired PaymentReceiptPdfService pdfService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody WaterBill bill) {
        try { return ResponseEntity.ok(billService.create(bill)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @GetMapping("/getall")
    public List<WaterBill> getall() { return billService.getall(); }

    @GetMapping("/lookup")
    public ResponseEntity<Object> lookup(@RequestParam String meterNo, @RequestParam String mobile) {
        try { return ResponseEntity.ok(billService.lookup(meterNo, mobile)); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        WaterBill b = billService.getById(id);
        return b == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(b);
    }

    @PostMapping("/pay/{id}")
    public ResponseEntity<Object> pay(@PathVariable int id, @RequestBody Map<String,String> body) {
        try { return ResponseEntity.ok(billService.payBill(id, body)); }
        catch (IllegalStateException e) { return ResponseEntity.status(409).body(Map.of("message", e.getMessage())); }
        catch (Exception e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    }

    @GetMapping("/receipt/{receiptId}/pdf")
    public ResponseEntity<byte[]> receiptPdf(@PathVariable int receiptId) {
        PaymentReceipt receipt = paymentService.getReceiptById(receiptId);
        if (receipt == null) return ResponseEntity.notFound().build();
        PaymentTransaction txn = paymentService.getById(receipt.getTxnId());
        if (txn == null || !"WaterBill".equals(txn.getServiceType())) return ResponseEntity.badRequest().build();
        WaterBill bill = billService.getById(txn.getServiceRefId());
        byte[] pdf = pdfService.generate(receipt, txn, bill != null ? bill.getAuthoritySignature() : null, bill != null ? bill.getAuthoritySeal() : null);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=water-bill-receipt-" + receipt.getReceiptNo() + ".pdf")
            .body(pdf);
    }

    @GetMapping("/receipt/no/{receiptNo}/pdf")
    public ResponseEntity<byte[]> receiptPdfByNo(@PathVariable String receiptNo) {
        PaymentReceipt receipt = paymentService.getReceiptByNo(receiptNo);
        if (receipt == null) return ResponseEntity.notFound().build();
        PaymentTransaction txn = paymentService.getById(receipt.getTxnId());
        if (txn == null || !"WaterBill".equals(txn.getServiceType())) return ResponseEntity.badRequest().build();
        WaterBill bill = billService.getById(txn.getServiceRefId());
        byte[] pdf = pdfService.generate(receipt, txn, bill != null ? bill.getAuthoritySignature() : null, bill != null ? bill.getAuthoritySeal() : null);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=water-bill-receipt-" + receipt.getReceiptNo() + ".pdf")
            .body(pdf);
    }

    @PutMapping("/authority-assets/{id}")
    public ResponseEntity<Object> updateAuthorityAssets(@PathVariable int id, @RequestBody Map<String,String> body) {
        try {
            WaterBill bill = billService.updateAuthorityAssets(id, body.get("signatureBase64"), body.get("sealBase64"));
            return ResponseEntity.ok(Map.of("message", "Water signature and seal saved", "signatureSaved", bill.getAuthoritySignature() != null, "sealSaved", bill.getAuthoritySeal() != null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String,String> body) {
        billService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable int id, @RequestBody WaterBill bill) {
        bill.setId(id); billService.update(bill);
        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        billService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
