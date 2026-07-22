package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.ConstructionDAO;
import com.mgt.model.Construction;

@Service
public class ConstructionService {

    @Autowired ConstructionDAO constructionDAO;
    @Autowired ApplicationEmailNotifier emailNotifier;

    public void create(Construction construction) {
        constructionDAO.save(construction);
        // আবেদন জমার confirmation email
        if (construction.getEmail() != null && !construction.getEmail().isBlank()) {
            emailNotifier.sendApplicationReceived(
                construction.getEmail(),
                construction.getApplicantName(),
                "নির্মাণ অনুমতি আবেদন",
                "CON-" + construction.getId()
            );
        }
    }

    public List<Construction> getall() { return constructionDAO.getall(); }

    public List<Construction> findByContact(String contact) {
        return constructionDAO.findByContact(contact);
    }

    public void updateStatus(int id, String status) {
        constructionDAO.updateStatus(id, status);
        // Status update email
        Construction con = constructionDAO.getById(id);
        if (con != null && con.getEmail() != null && !con.getEmail().isBlank()) {
            emailNotifier.sendStatusUpdate(
                con.getEmail(), con.getApplicantName(),
                "নির্মাণ অনুমতি আবেদন", "CON-" + id,
                status, null
            );
        }
    }

    public void updateLocation(int id, Double lat, Double lng) {
        constructionDAO.updateLocation(id, lat, lng);
    }
}
