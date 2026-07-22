package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.TaxAssessmentDAO;
import com.mgt.model.TaxAssessment;

@Service
public class TaxAssessmentService {

    @Autowired
    private TaxAssessmentDAO dao;

    public TaxAssessment create(TaxAssessment assessment) {
        if (assessment.getTaxAmount() == null && assessment.getArea() != null && assessment.getRate() != null) {
            double tax   = assessment.getArea() * assessment.getRate();
            double total = tax + (assessment.getPreviousDue() != null ? assessment.getPreviousDue() : 0.0);
            assessment.setTaxAmount(tax);
            assessment.setTotalPayable(total);
        }
        dao.save(assessment);
        return assessment;
    }

    public List<TaxAssessment> getAll() {
        return dao.getAll();
    }

    public TaxAssessment getById(int id) {
        return dao.getById(id);
    }

    public List<TaxAssessment> getByHoldingNo(String holdingNo) {
        return dao.getByHoldingNo(holdingNo);
    }

    public void updateStatus(int id, String status) {
        dao.updateStatus(id, status);
    }

    public void delete(int id) {
        dao.delete(id);
    }
}
