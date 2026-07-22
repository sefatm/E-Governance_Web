package com.mgt.service;

import com.mgt.dao.FarmerCardDAO;
import com.mgt.model.FarmerCard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class FarmerCardService {

    @Autowired FarmerCardDAO            farmerCardDAO;
    @Autowired ApplicationEmailNotifier emailNotifier;

    // ── CREATE ────────────────────────────────────────────────
    public String create(FarmerCard card) {
        if (farmerCardDAO.getByNid(card.getNid()) != null) return "DUPLICATE";
        farmerCardDAO.create(card);
        emailNotifier.sendApplicationReceived(
                card.getContact(), card.getFarmerName(),
                "কৃষক কার্ড", card.getCardNo()
        );
        return "OK";
    }

    // ── UPDATE ────────────────────────────────────────────────
    public void updateOnly(FarmerCard card) {
        farmerCardDAO.updateOnly(card);
    }

    // ── READ ──────────────────────────────────────────────────
    public void updateStatus(int id, String status, String by, String reason) {
        updateStatus(id, status, by, reason, null);
    }

    public List<FarmerCard> getAll()                       { return farmerCardDAO.getAll(); }
    public List<FarmerCard> getByStatus(String s)          { return farmerCardDAO.getByStatus(s); }
    public List<FarmerCard> getByDistrict(String d)        { return farmerCardDAO.getByDistrict(d); }
    public List<FarmerCard> getByWard(String w)            { return farmerCardDAO.getByWard(w); }
    public FarmerCard       getById(int id)                { return farmerCardDAO.getById(id); }
    public FarmerCard       getByNid(String nid)           { return farmerCardDAO.getByNid(nid); }
    public FarmerCard       getByCardNo(String cardNo)     { return farmerCardDAO.getByCardNo(cardNo); }

    // ── STATUS ────────────────────────────────────────────────
    public void updateStatus(int id, String status, String by, String reason, String signatureBase64) {
        farmerCardDAO.updateStatus(id, status, by, reason);
        if (signatureBase64 != null && !signatureBase64.isBlank()) {
            FarmerCard card = farmerCardDAO.getById(id);
            if (card != null) {
                card.setCertificateSignature(signatureBase64);
                farmerCardDAO.updateOnly(card);
            }
        }
        FarmerCard card = farmerCardDAO.getById(id);
        if (card != null) {
            if ("Approved".equalsIgnoreCase(status)) {
                // ✅ Approval — ৭ কর্মদিবসের মধ্যে কার্ড সংগ্রহের নির্দেশ
                emailNotifier.sendCardApprovedWithPickupInfo(
                        card.getContact(),
                        card.getFarmerName(),
                        "কৃষক কার্ড",
                        card.getCardNo(),
                        null, null
                );
            } else {
                emailNotifier.sendStatusUpdate(
                        card.getContact(), card.getFarmerName(),
                        "কৃষক কার্ড", card.getCardNo(), status, reason
                );
            }
        }
    }

    // ── DUPLICATE DETECTION ───────────────────────────────────
    public List<Object[]> checkDuplicateAcrossCards(String nid) {
        return farmerCardDAO.checkDuplicateAcrossCards(nid);
    }

    // ── LAND VERIFICATION ─────────────────────────────────────
    public void verifyLand(int id, String officer) {
        farmerCardDAO.verifyLand(id, officer);
    }

    public void unverifyLand(int id) {
        farmerCardDAO.unverifyLand(id);
    }

    // ── OFFICER ASSIGNMENT ────────────────────────────────────
    public void assignOfficer(int id, String officer) {
        farmerCardDAO.assignOfficer(id, officer);
    }

    // ── RENEWAL ──────────────────────────────────────────────
    public void renew(int id) {
        farmerCardDAO.renew(id);
    }

    public List<FarmerCard> getExpiringSoon(int days) {
        return farmerCardDAO.getExpiringSoon(days);
    }

    // ── BULK APPROVE ──────────────────────────────────────────
    public Map<String, Object> bulkApproveByWard(String ward, String approvedBy) {
        int count = farmerCardDAO.bulkApproveByWard(ward, approvedBy);
        return Map.of("message", "Ward " + ward + " এর " + count + "টি কার্ড অনুমোদিত হয়েছে।", "count", count);
    }

    // ── DELETE ────────────────────────────────────────────────
    public void delete(int id) { farmerCardDAO.delete(id); }
}
