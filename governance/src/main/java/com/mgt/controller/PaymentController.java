package com.mgt.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.PaymentTransaction;
import com.mgt.model.PaymentReceipt;
import com.mgt.model.TaxPayment;
import com.mgt.service.PaymentEmailService;
import com.mgt.service.PaymentReceiptPdfService;
import com.mgt.service.PaymentService;
import com.mgt.service.TaxPaymentService;
import com.mgt.service.WaterBillService;

@RestController
@RequestMapping(value = "/api/payment")
public class PaymentController {

    @Autowired
    PaymentService paymentService;

    @Autowired
    TaxPaymentService taxPaymentService;

    @Autowired
    PaymentReceiptPdfService pdfService;

    @Autowired
    PaymentEmailService paymentEmailService;

    @Autowired
    WaterBillService waterBillService;

    // POST /api/payment/initiate
    @PostMapping("/initiate")
    public ResponseEntity<Object> initiate(@RequestBody PaymentTransaction txn) {
        try {
            PaymentTransaction saved = paymentService.initiate(txn);
            // Frontend এই txnId দিয়ে confirm call করবে
            return ResponseEntity.ok(Map.of(
                "txn", saved,
                "gatewayUrl", "internal" // same-page confirm flow
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // PUT /api/payment/confirm/{id}
    // Payment confirm করে। HoldingTax হলে tax_payment table-এও save করে।
    @PutMapping("/confirm/{id}")
    public ResponseEntity<Object> confirm(@PathVariable int id,
                                          @RequestBody Map<String, String> body) {
        try {
            PaymentTransaction txn = paymentService.confirm(id, body.get("providerTxnId"));

            if ("HoldingTax".equals(txn.getServiceType())) {
                // FIX: Try holdingNo field first, fallback to parsing description
                String holdingNo = txn.getHoldingNo() != null && !txn.getHoldingNo().isBlank()
                        ? txn.getHoldingNo()
                        : parseHoldingNo(txn.getDescription());

                if (holdingNo != null) {
                    TaxPayment tp = new TaxPayment();
                    tp.setHoldingNo(holdingNo);
                    tp.setOwnerName(txn.getCitizenName());
                    tp.setAmount(txn.getAmount());
                    tp.setMethod(txn.getMethod());
                    tp.setTxnId(txn.getTxnRef());
                    tp.setPaymentDate(LocalDate.now());
                    tp.setStatus("Paid");
                    taxPaymentService.create(tp);
                } else {
                    System.err.println("[PaymentController] WARNING: HoldingTax confirmed but holdingNo could not be parsed. txnId=" + id + ", description=" + txn.getDescription());
                }
            }

            // Receipt fetch and return
            PaymentReceipt receipt = paymentService.getReceiptByTxnId(txn.getId());

            if ("WaterBill".equals(txn.getServiceType())) {
                waterBillService.markPaidFromTransaction(txn, receipt);
            }

            // Email পাঠাও — @Async তাই HTTP response block হবে না
            if (receipt != null) {
                paymentEmailService.sendReceiptEmail(receipt, txn);
            }

            return ResponseEntity.ok(Map.of("txn", txn, "receipt", receipt != null ? receipt : Map.of()));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // "Holding No: H-001 | Owner: ..." format থেকে holding no parse
    private String parseHoldingNo(String desc) {
        if (desc == null) return null;
        try {
            if (desc.contains("Holding No:")) {
                String part = desc.split("Holding No:")[1].trim();
                return part.contains("|") ? part.split("\\|")[0].trim() : part.trim();
            }
        } catch (Exception ignored) {}
        return null;
    }

    // PUT /api/payment/fail/{id}
    @PutMapping("/fail/{id}")
    public ResponseEntity<Void> fail(@PathVariable int id, @RequestBody Map<String, String> body) {
        paymentService.fail(id, body.get("reason"));
        return ResponseEntity.ok().build();
    }

    // PUT /api/payment/refund/{id}
    @PutMapping("/refund/{id}")
    public ResponseEntity<Void> refund(@PathVariable int id) {
        paymentService.refund(id);
        return ResponseEntity.ok().build();
    }

    // GET /api/payment/transactions
    @GetMapping("/transactions")
    public List<PaymentTransaction> getAll() {
        return paymentService.getAll();
    }

    // GET /api/payment/transactions/{id}
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        PaymentTransaction t = paymentService.getById(id);
        if (t == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(t);
    }

    // GET /api/payment/transactions/citizen/{nid}
    @GetMapping("/transactions/citizen/{nid}")
    public List<PaymentTransaction> getByNid(@PathVariable String nid) {
        return paymentService.getByNid(nid);
    }

    // GET /api/payment/transactions/status/{status}
    @GetMapping("/transactions/status/{status}")
    public List<PaymentTransaction> getByStatus(@PathVariable String status) {
        return paymentService.getByStatus(status);
    }

    // GET /api/payment/receipts
    @GetMapping("/receipts")
    public List<PaymentReceipt> getAllReceipts() {
        return paymentService.getAllReceipts();
    }

    // GET /api/payment/receipts/txn/{txnId}
    @GetMapping("/receipts/txn/{txnId}")
    public ResponseEntity<Object> getReceiptByTxn(@PathVariable int txnId) {
        PaymentReceipt r = paymentService.getReceiptByTxnId(txnId);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    // GET /api/payment/receipts/citizen/{nid}
    @GetMapping("/receipts/citizen/{nid}")
    public List<PaymentReceipt> getReceiptsByNid(@PathVariable String nid) {
        return paymentService.getReceiptsByNid(nid);
    }

    // GET /api/payment/receipts/verify/{receiptNo}
    @GetMapping("/receipts/verify/{receiptNo}")
    public ResponseEntity<Object> verifyReceipt(@PathVariable String receiptNo) {
        PaymentReceipt r = paymentService.getReceiptByNo(receiptNo);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    // GET /api/payment/receipts/pdf/{receiptId}
    // Receipt PDF download করো — frontend এই URL-এ GET করলে PDF পাবে
    @GetMapping("/receipts/pdf/{receiptId}")
    public ResponseEntity<byte[]> downloadReceiptPdf(@PathVariable int receiptId) {
        PaymentReceipt receipt = paymentService.getReceiptById(receiptId);
        if (receipt == null) return ResponseEntity.notFound().build();

        PaymentTransaction txn = paymentService.getById(receipt.getTxnId());
        byte[] pdf = pdfService.generate(receipt, txn);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"receipt-" + receipt.getReceiptNo() + ".pdf\"")
            .body(pdf);
    }

    // GET /api/payment/receipts/pdf/txn/{txnId}
    // Transaction ID দিয়ে receipt PDF download
    @GetMapping("/receipts/pdf/txn/{txnId}")
    public ResponseEntity<byte[]> downloadReceiptPdfByTxn(@PathVariable int txnId) {
        PaymentReceipt receipt = paymentService.getReceiptByTxnId(txnId);
        if (receipt == null) return ResponseEntity.notFound().build();

        PaymentTransaction txn = paymentService.getById(txnId);
        byte[] pdf = pdfService.generate(receipt, txn);

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"receipt-" + receipt.getReceiptNo() + ".pdf\"")
            .body(pdf);
    }

    // GET /api/payment/summary
    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        return paymentService.getSummary();
    }
}
