package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.PickupRequestDAO;
import com.mgt.model.PickupRequest;
import com.mgt.model.WasteCollectionLog;

@Service
public class PickupRequestService {

    @Autowired PickupRequestDAO wasteDAO;
    @Autowired ApplicationEmailNotifier emailNotifier;
    @Autowired WasteCollectionLogService collectionLogService;

    public void create(PickupRequest waste) {
        if (waste.getStatus() == null || waste.getStatus().isBlank()) waste.setStatus("Pending");
        wasteDAO.save(waste);
        // আবেদন জমার confirmation email
        if (waste.getEmail() != null && !waste.getEmail().isBlank()) {
            emailNotifier.sendApplicationReceived(
                waste.getEmail(),
                waste.getName(),
                "বর্জ্য সংগ্রহ অনুরোধ",
                "WR-" + waste.getId()
            );
        }
    }

    public List<PickupRequest> getall()       { return wasteDAO.getall(); }
    public List<PickupRequest> findByPhone(String phone) { return wasteDAO.findByPhone(phone); }
    public PickupRequest getById(int id)       { return wasteDAO.getById(id); }

    public void updateStatus(int id, String status) {
        wasteDAO.updateStatus(id, status);
        PickupRequest wr = wasteDAO.getById(id);
        if (wr != null && ("Collected".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status))
                && !collectionLogService.existsForPickup(id)) {
            WasteCollectionLog log = new WasteCollectionLog();
            log.setPickupRequestId(id);
            log.setWard(wr.getWard());
            log.setArea(wr.getAddress());
            log.setWasteType(wr.getType());
            log.setStatus("Completed");
            collectionLogService.create(log);
        }
        // Status update email
        if (wr != null && wr.getEmail() != null && !wr.getEmail().isBlank()) {
            emailNotifier.sendStatusUpdate(
                wr.getEmail(), wr.getName(),
                "বর্জ্য সংগ্রহ অনুরোধ", "WR-" + id,
                status, null
            );
        }
    }

    public void update(PickupRequest waste) { wasteDAO.update(waste); }
    public void delete(int id)               { wasteDAO.delete(id); }
}
