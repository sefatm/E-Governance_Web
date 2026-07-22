package com.mgt.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.VgdCardDAO;
import com.mgt.model.VgdCard;

@Service
public class VgdCardService {

    @Autowired VgdCardDAO vgdCardDAO;
    @Autowired ApplicationEmailNotifier emailNotifier;

    public String create(VgdCard card) {
        // Eligibility: land > 0.5 acre → disqualified
        if (card.isHasLand() && card.getLandArea() != null
                && card.getLandArea().doubleValue() > 0.5)
            return "LAND_EXCEEDED";

        if (vgdCardDAO.getByNid(card.getNid()) != null)
            return "DUPLICATE";

        vgdCardDAO.save(card);
        emailNotifier.sendApplicationReceived(
                card.getContact(), card.getHolderName(),
                card.getCardType() + " কার্ড", card.getCardNo()
        );
        return "OK";
    }

    public void updateStatus(int id, String status, String by, String reason) {
        updateStatus(id, status, by, reason, null);
    }

    public List<VgdCard> getAll()                       { return vgdCardDAO.getAll(); }
    public List<VgdCard> getByStatus(String s)          { return vgdCardDAO.getByStatus(s); }
    public List<VgdCard> getByCardType(String t)        { return vgdCardDAO.getByCardType(t); }
    public List<VgdCard> getByWard(String w)            { return vgdCardDAO.getByWard(w); }
    public List<VgdCard> getExpiringSoon(int days)      { return vgdCardDAO.getExpiringSoon(days); }
    public VgdCard       getById(int id)                { return vgdCardDAO.getById(id); }
    public VgdCard       getByNid(String nid)           { return vgdCardDAO.getByNid(nid); }

    public void updateStatus(int id, String status, String by, String reason, String signatureBase64) {
        vgdCardDAO.updateStatus(id, status, by, reason);
        if (signatureBase64 != null && !signatureBase64.isBlank()) {
            VgdCard card = vgdCardDAO.getById(id);
            if (card != null) {
                card.setCertificateSignature(signatureBase64);
                vgdCardDAO.updateOnly(card);
            }
        }
        VgdCard card = vgdCardDAO.getById(id);
        if (card != null)
            if ("Approved".equalsIgnoreCase(status)) {
                // ✅ Approval — ৭ কর্মদিবসের মধ্যে কার্ড সংগ্রহের নির্দেশ
                emailNotifier.sendCardApprovedWithPickupInfo(
                        card.getContact(),
                        card.getHolderName(),
                        card.getCardType() + " কার্ড",
                        card.getCardNo(),
                        null, null
                );
            } else {
                emailNotifier.sendStatusUpdate(
                        card.getContact(), card.getHolderName(),
                        card.getCardType() + " কার্ড", card.getCardNo(), status, reason
                );
            }
    }

    /**
     * ✅ FIX Bug 6: now inserts into vgd_distribution table
     * Returns a result map with success/message
     */
    public Map<String, Object> recordDistribution(int id, String distMonth,
                                                   String distributedBy, String remarks) {
        String result = vgdCardDAO.recordDistribution(id, distMonth, distributedBy, remarks);
        return switch (result) {
            case "OK" -> {
                // ✅ Distribution confirmation email to citizen
                VgdCard card = vgdCardDAO.getById(id);
                if (card != null) {
                    StringBuilder items = new StringBuilder();
                    if (card.getMonthlyRiceKg() != null && card.getMonthlyRiceKg().doubleValue() > 0)
                        items.append("চাল: ").append(card.getMonthlyRiceKg()).append(" কেজি");
                    if (card.getMonthlyWheatKg() != null && card.getMonthlyWheatKg().doubleValue() > 0) {
                        if (items.length() > 0) items.append(", ");
                        items.append("গম: ").append(card.getMonthlyWheatKg()).append(" কেজি");
                    }
                    if (card.getCashAmount() != null && card.getCashAmount().doubleValue() > 0) {
                        if (items.length() > 0) items.append(", ");
                        items.append("নগদ: ৳").append(card.getCashAmount());
                    }
                    emailNotifier.sendDistributionConfirmation(
                            card.getContact(), card.getHolderName(), card.getCardNo(),
                            card.getCardType() + " কার্ড", distMonth,
                            items.length() > 0 ? items.toString() : "সুবিধা প্রদান করা হয়েছে",
                            distributedBy
                    );
                }
                yield Map.of("success", true, "message", "বিতরণ রেকর্ড সফলভাবে হয়েছে।");
            }
            case "ALREADY_DISTRIBUTED" -> Map.of("success", false, "message", distMonth + " মাসে ইতিমধ্যে বিতরণ রেকর্ড করা হয়েছে।");
            case "NOT_APPROVED"      -> Map.of("success", false, "message", "কার্ডটি অনুমোদিত নয়।");
            case "NOT_FOUND"         -> Map.of("success", false, "message", "কার্ড পাওয়া যায়নি।");
            default                  -> Map.of("success", false, "message", result);
        };
    }

    public List<Object[]> getDistributionHistory(int id) {
        return vgdCardDAO.getDistributionHistory(id);
    }

    public void renew(int id) { vgdCardDAO.renew(id); }
    public void delete(int id) { vgdCardDAO.delete(id); }
}
