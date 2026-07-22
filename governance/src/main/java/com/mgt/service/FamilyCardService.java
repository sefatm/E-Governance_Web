package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.FamilyCardDAO;
import com.mgt.model.FamilyCard;

@Service
public class FamilyCardService {

    @Autowired
    FamilyCardDAO familyCardDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public String create(FamilyCard card) {
        FamilyCard existing = familyCardDAO.getByNid(card.getNid());
        if (existing != null) return "DUPLICATE";
        familyCardDAO.save(card);

        // আবেদন জমার notification
        emailNotifier.sendApplicationReceived(
                card.getContact(),
                card.getHolderName(),
                "পারিবারিক কার্ড",
                card.getCardNo()
        );
        return "OK";
    }

    public void updateStatus(int id, String status, String approvedBy, String rejectionReason) {
        updateStatus(id, status, approvedBy, rejectionReason, null);
    }

    public List<FamilyCard> getAll() {
        return familyCardDAO.getAll();
    }

    public List<FamilyCard> getByStatus(String status) {
        return familyCardDAO.getByStatus(status);
    }

    public FamilyCard getById(int id) {
        return familyCardDAO.getById(id);
    }

    public FamilyCard getByNid(String nid) {
        return familyCardDAO.getByNid(nid);
    }

    public void updateStatus(int id, String status, String approvedBy, String rejectionReason, String signatureBase64) {
        familyCardDAO.updateStatus(id, status, approvedBy, rejectionReason);
        if (signatureBase64 != null && !signatureBase64.isBlank()) {
            FamilyCard card = familyCardDAO.getById(id);
            if (card != null) {
                card.setCertificateSignature(signatureBase64);
                familyCardDAO.updateOnly(card);
            }
        }

        FamilyCard card = familyCardDAO.getById(id);
        if (card != null) {
            if ("Approved".equalsIgnoreCase(status)) {
                // ✅ Approval — ৭ কর্মদিবসের মধ্যে কার্ড সংগ্রহের নির্দেশ
                emailNotifier.sendCardApprovedWithPickupInfo(
                        card.getContact(),
                        card.getHolderName(),
                        "পারিবারিক কার্ড",
                        card.getCardNo(),
                        null, null
                );
            } else {
                emailNotifier.sendStatusUpdate(
                        card.getContact(),
                        card.getHolderName(),
                        "পারিবারিক কার্ড",
                        card.getCardNo(),
                        status, rejectionReason
                );
            }
        }
    }

    public void delete(int id) {
        familyCardDAO.delete(id);
    }
}
