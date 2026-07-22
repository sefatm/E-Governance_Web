package com.mgt.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.PaymentTransactionDAO;
import com.mgt.dao.PaymentReceiptDAO;
import com.mgt.model.PaymentTransaction;
import com.mgt.model.PaymentReceipt;

@Service
public class PaymentService {

    @Autowired
    PaymentTransactionDAO txnDAO;

    @Autowired
    PaymentReceiptDAO receiptDAO;

    public PaymentTransaction initiate(PaymentTransaction txn) {

        if (txn.getCitizenNid() == null || txn.getCitizenNid().isBlank())
            throw new RuntimeException("NID is required.");
        if (txn.getAmount() <= 0)
            throw new RuntimeException("Amount must be greater than 0.");
        if (txn.getMethod() == null || txn.getMethod().isBlank())
            throw new RuntimeException("Payment method is required.");

        String date   = java.time.LocalDate.now().toString().replace("-", "");
        String txnRef = "TXN-" + date + "-" + (System.currentTimeMillis() % 100000);

        txn.setTxnRef(txnRef);
        txn.setStatus("Pending");
        txn.setCreatedAt(LocalDateTime.now());

        return txnDAO.save(txn);
    }

    public PaymentTransaction confirm(int id, String providerTxnId) {
        PaymentTransaction txn = txnDAO.getById(id);
        if (txn == null)
            throw new RuntimeException("Transaction not found.");
        if (!"Pending".equals(txn.getStatus()))
            throw new RuntimeException("Transaction is already " + txn.getStatus() + ".");
        if (providerTxnId == null || providerTxnId.isBlank())
            throw new RuntimeException("Provider transaction ID is required.");

        txn.setStatus("Completed");
        txn.setProviderTxnId(providerTxnId.trim());
        txn.setPaidAt(LocalDateTime.now());
        PaymentTransaction saved = txnDAO.update(txn);

        issueReceipt(saved);
        return saved;
    }

    public void fail(int id, String reason) {
        PaymentTransaction txn = txnDAO.getById(id);
        if (txn == null) throw new RuntimeException("Transaction not found.");
        txn.setStatus("Failed");
        txn.setFailureReason(reason);
        txnDAO.update(txn);
    }

    public void refund(int id) {
        PaymentTransaction txn = txnDAO.getById(id);
        if (txn == null) throw new RuntimeException("Transaction not found.");
        if (!"Completed".equals(txn.getStatus()))
            throw new RuntimeException("Only completed payments can be refunded.");
        txn.setStatus("Refunded");
        txnDAO.update(txn);
    }

    private void issueReceipt(PaymentTransaction txn) {
        if (receiptDAO.getByTxnId(txn.getId()) != null) return;

        String date      = java.time.LocalDate.now().toString().replace("-", "");
        String receiptNo = "RCP-" + date + "-" + (System.currentTimeMillis() % 100000);

        PaymentReceipt receipt = new PaymentReceipt();
        receipt.setReceiptNo(receiptNo);
        receipt.setTxnId(txn.getId());
        receipt.setCitizenNid(txn.getCitizenNid());
        receipt.setCitizenName(txn.getCitizenName());
        receipt.setServiceType(txn.getServiceType());
        receipt.setDescription(txn.getDescription());
        receipt.setAmount(txn.getAmount());
        receipt.setMethod(txn.getMethod());
        receipt.setIssuedAt(LocalDateTime.now());
        receiptDAO.save(receipt);
    }

    public List<PaymentTransaction> getAll() {
        return txnDAO.getAll();
    }

    public PaymentTransaction getById(int id) {
        return txnDAO.getById(id);
    }

    public List<PaymentTransaction> getByNid(String nid) {
        return txnDAO.getByCitizenNid(nid);
    }

    public List<PaymentTransaction> getByStatus(String status) {
        return txnDAO.getByStatus(status);
    }

    public List<PaymentReceipt> getAllReceipts() {
        return receiptDAO.getAll();
    }

    // Receipt ID দিয়ে fetch — PDF download endpoint ব্যবহার করে
    public PaymentReceipt getReceiptById(int id) {
        return receiptDAO.getById(id);
    }

    public PaymentReceipt getReceiptByTxnId(int txnId) {
        return receiptDAO.getByTxnId(txnId);
    }

    public List<PaymentReceipt> getReceiptsByNid(String nid) {
        return receiptDAO.getByCitizenNid(nid);
    }

    public PaymentReceipt getReceiptByNo(String receiptNo) {
        return receiptDAO.getByReceiptNo(receiptNo);
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> map = new HashMap<>();
        map.put("totalCollected", txnDAO.getTotalCollected());
        map.put("completed",      txnDAO.countByStatus("Completed"));
        map.put("pending",        txnDAO.countByStatus("Pending"));
        map.put("failed",         txnDAO.countByStatus("Failed"));
        map.put("refunded",       txnDAO.countByStatus("Refunded"));
        return map;
    }
}
