package com.mgt.service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mgt.dao.WaterBillDAO;
import com.mgt.model.PaymentReceipt;
import com.mgt.model.PaymentTransaction;
import com.mgt.model.WaterBill;

@Service
public class WaterBillService {

    @Autowired WaterBillDAO billDAO;
    @Autowired ApplicationEmailNotifier emailNotifier;
    @Autowired PaymentService paymentService;
    @Autowired PaymentEmailService paymentEmailService;

    @Transactional
    public WaterBill create(WaterBill bill) {
        int units = bill.getCurrentReading() - bill.getPreviousReading();
        if (units < 0) throw new IllegalArgumentException("Current reading cannot be lower than previous reading.");

        double rate = switch (bill.getConnectionType() == null ? "Residential" : bill.getConnectionType()) {
            case "Commercial" -> 0.30;
            case "Industrial" -> 0.50;
            case "Government" -> 0.10;
            default -> 0.15;
        };
        double base = switch (bill.getConnectionType() == null ? "Residential" : bill.getConnectionType()) {
            case "Commercial" -> 150.0;
            case "Industrial" -> 300.0;
            case "Government" -> 30.0;
            default -> 50.0;
        };

        bill.setUnits(units);
        String type = bill.getBillType() != null ? bill.getBillType() : "Auto";
        double amount;
        if ("Fixed".equals(type)) amount = 500.0;
        else if ("Manual".equals(type)) amount = bill.getAmount();
        else {
            double subtotal = units * rate;
            amount = subtotal + (subtotal * 0.10) + base;
        }
        bill.setAmount(Math.round(amount * 100.0) / 100.0);
        bill.setStatus("Unpaid");
        bill.setCreatedAt(LocalDateTime.now());
        billDAO.save(bill);

        if (bill.getEmail() != null && !bill.getEmail().isBlank()) {
            emailNotifier.sendApplicationReceived(
                bill.getEmail(), bill.getName(),
                "পানি বিল — " + bill.getMonth(), "WB-" + bill.getId());
        }
        return bill;
    }

    public List<WaterBill> lookup(String meterNo, String mobile) {
        if (meterNo == null || meterNo.isBlank()) throw new IllegalArgumentException("Meter number is required.");
        if (mobile == null || mobile.isBlank()) throw new IllegalArgumentException("Mobile number is required.");
        return billDAO.findByMeterAndMobile(meterNo.trim(), mobile.trim());
    }

    @Transactional
    public Map<String,Object> payBill(int id, Map<String,String> body) {
        WaterBill bill = billDAO.getById(id);
        if (bill == null) throw new IllegalArgumentException("Water bill not found.");
        if ("Paid".equalsIgnoreCase(bill.getStatus())) throw new IllegalStateException("This bill is already paid.");

        String method = value(body, "method", "Mobile Banking");
        String nid = value(body, "nid", bill.getNid());
        String mobile = value(body, "mobile", bill.getMobile());
        String email = value(body, "email", bill.getEmail());
        if (nid == null || nid.isBlank()) throw new IllegalArgumentException("NID is required for payment receipt.");

        PaymentTransaction txn = new PaymentTransaction();
        txn.setCitizenNid(nid);
        txn.setCitizenName(bill.getName());
        txn.setMobile(mobile);
        txn.setEmail(email);
        txn.setServiceType("WaterBill");
        txn.setServiceRefId(bill.getId());
        txn.setDescription("Water Bill | Meter: " + bill.getMeterNo() + " | Month: " + bill.getMonth());
        txn.setAmount(bill.getAmount());
        txn.setMethod(method);

        PaymentTransaction initiated = paymentService.initiate(txn);
        String providerTxnId = value(body, "providerTxnId", "DEMO-WB-" + System.currentTimeMillis());
        PaymentTransaction completed = paymentService.confirm(initiated.getId(), providerTxnId);
        PaymentReceipt receipt = paymentService.getReceiptByTxnId(completed.getId());

        bill.setStatus("Paid");
        bill.setPaymentMethod(method);
        bill.setTxnRef(completed.getTxnRef());
        bill.setReceiptNo(receipt != null ? receipt.getReceiptNo() : null);
        bill.setPaidAt(LocalDateTime.now());
        if (email != null && !email.isBlank()) bill.setEmail(email);
        if (mobile != null && !mobile.isBlank()) bill.setMobile(mobile);
        bill.setNid(nid);
        billDAO.update(bill);

        if (receipt != null) paymentEmailService.sendReceiptEmail(receipt, completed);

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("message", "Water bill payment completed successfully");
        result.put("bill", bill);
        result.put("txn", completed);
        result.put("receipt", receipt);
        return result;
    }

    private String value(Map<String,String> body, String key, String fallback) {
        String v = body == null ? null : body.get(key);
        return (v == null || v.isBlank()) ? fallback : v.trim();
    }

    public List<WaterBill> getall() { return billDAO.getall(); }
    public WaterBill getById(int id) { return billDAO.getById(id); }
    public void updateStatus(int id, String s) { billDAO.updateStatus(id, s); }
    public void update(WaterBill bill) { billDAO.update(bill); }

    @Transactional
    public WaterBill markPaidFromTransaction(PaymentTransaction txn, PaymentReceipt receipt) {
        if (txn == null || txn.getServiceRefId() == null) {
            throw new IllegalArgumentException("Water bill reference is missing.");
        }
        WaterBill bill = billDAO.getById(txn.getServiceRefId());
        if (bill == null) throw new IllegalArgumentException("Water bill not found.");
        bill.setStatus("Paid");
        bill.setPaymentMethod(txn.getMethod());
        bill.setTxnRef(txn.getTxnRef());
        bill.setReceiptNo(receipt != null ? receipt.getReceiptNo() : bill.getReceiptNo());
        bill.setPaidAt(txn.getPaidAt() != null ? txn.getPaidAt() : LocalDateTime.now());
        if (txn.getEmail() != null && !txn.getEmail().isBlank()) bill.setEmail(txn.getEmail());
        if (txn.getMobile() != null && !txn.getMobile().isBlank()) bill.setMobile(txn.getMobile());
        if (txn.getCitizenNid() != null && !txn.getCitizenNid().isBlank()) bill.setNid(txn.getCitizenNid());
        billDAO.update(bill);
        return bill;
    }

    public WaterBill updateAuthorityAssets(int id, String signatureBase64, String sealBase64) {
        if (signatureBase64 == null || signatureBase64.isBlank()) throw new IllegalArgumentException("Signature is required");
        if (sealBase64 == null || sealBase64.isBlank()) throw new IllegalArgumentException("Seal is required");
        WaterBill bill = billDAO.getById(id);
        if (bill == null) throw new IllegalArgumentException("Water bill not found.");
        bill.setAuthoritySignature(signatureBase64);
        bill.setAuthoritySeal(sealBase64);
        billDAO.update(bill);
        return bill;
    }
    public void delete(int id) { billDAO.delete(id); }
}
