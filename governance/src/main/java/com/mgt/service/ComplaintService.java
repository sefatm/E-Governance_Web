package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.ComplaintDAO;
import com.mgt.model.Complaint;

@Service
public class ComplaintService {

    @Autowired ComplaintDAO complaintDAO;
    @Autowired ApplicationEmailNotifier emailNotifier;

    public void create(Complaint complaint) {
        complaintDAO.save(complaint);
        // অভিযোগ জমার confirmation email
        if (complaint.getEmail() != null && !complaint.getEmail().isBlank()) {
            emailNotifier.sendApplicationReceived(
                complaint.getEmail(),
                complaint.getName(),
                "নাগরিক অভিযোগ",
                "CMP-" + complaint.getId()
            );
        }
    }

    public List<Complaint> getall() { return complaintDAO.getall(); }

    public List<Complaint> getByContact(String contact) {
        return complaintDAO.findByContact(contact);
    }

    public void updateStatus(int id, String status) {
        complaintDAO.updateStatus(id, status);
        // Status update email
        Complaint c = complaintDAO.findById(id);
        if (c != null && c.getEmail() != null && !c.getEmail().isBlank()) {
            emailNotifier.sendStatusUpdate(
                c.getEmail(), c.getName(),
                "নাগরিক অভিযোগ", "CMP-" + id,
                status, null
            );
        }
    }

    public void updateLocation(int id, Double lat, Double lng) {
        complaintDAO.updateLocation(id, lat, lng);
    }

    public void updateRemarks(int id, String remarks) {
        complaintDAO.updateRemarks(id, remarks);
    }
}
