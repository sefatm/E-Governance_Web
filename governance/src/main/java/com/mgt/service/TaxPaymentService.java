package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.TaxPaymentDAO;
import com.mgt.model.TaxPayment;

@Service
public class TaxPaymentService {

    @Autowired private TaxPaymentDAO dao;
    @Autowired private ApplicationEmailNotifier emailNotifier;

    public TaxPayment create(TaxPayment payment) {
        dao.save(payment);
        // কর পরিশোধের confirmation email
        if (payment.getEmail() != null && !payment.getEmail().isBlank()) {
            emailNotifier.sendApplicationReceived(
                payment.getEmail(),
                payment.getOwnerName(),
                "হোল্ডিং কর পরিশোধ",
                payment.getTxnId() != null ? payment.getTxnId() : "TAX-" + payment.getId()
            );
        }
        return payment;
    }

    public List<TaxPayment> getAll()                              { return dao.getAll(); }
    public TaxPayment getById(int id)                             { return dao.getById(id); }
    public List<TaxPayment> getByHoldingNo(String holdingNo)      { return dao.getByHoldingNo(holdingNo); }
    public Double getTotalPaidByHoldingNo(String holdingNo)       { return dao.getTotalPaidByHoldingNo(holdingNo); }

    public void updateStatus(int id, String status) {
        dao.updateStatus(id, status);
        // Status update email
        TaxPayment tp = dao.getById(id);
        if (tp != null && tp.getEmail() != null && !tp.getEmail().isBlank()) {
            emailNotifier.sendStatusUpdate(
                tp.getEmail(), tp.getOwnerName(),
                "হোল্ডিং কর পরিশোধ",
                tp.getTxnId() != null ? tp.getTxnId() : "TAX-" + id,
                status, null
            );
        }
    }

    public void delete(int id) { dao.delete(id); }
}
