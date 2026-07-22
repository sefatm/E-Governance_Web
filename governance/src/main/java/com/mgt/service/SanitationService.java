package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.SanitationDAO;
import com.mgt.model.Sanitation;

@Service
public class SanitationService {

    @Autowired
    SanitationDAO sanitationDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public void create(Sanitation sanitation) {
        sanitationDAO.save(sanitation);

        // অভিযোগ জমার notification
        emailNotifier.sendApplicationReceived(
                sanitation.getEmail(),
                sanitation.getName() != null ? sanitation.getName() : "আবেদনকারী",
                "স্যানিটেশন অভিযোগ",
                String.valueOf(sanitation.getId())
        );
    }

    public List<Sanitation> getall() {
        return sanitationDAO.getall();
    }

    public Sanitation getById(int id) {
        return sanitationDAO.getById(id);
    }

    public void updateStatus(int id, String status) {
        sanitationDAO.updateStatus(id, status);

        // Resolved / Rejected notification
        Sanitation s = sanitationDAO.getById(id);
        if (s != null) {
            emailNotifier.sendStatusUpdate(
                    s.getEmail(),
                    s.getName() != null ? s.getName() : "আবেদনকারী",
                    "স্যানিটেশন অভিযোগ",
                    String.valueOf(id),
                    status,
                    null
            );
        }
    }

    public void delete(int id) {
        sanitationDAO.delete(id);
    }
}
