package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.RoadDAO;
import com.mgt.model.Road;

@Service
public class RoadService {

    @Autowired
    RoadDAO roadDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public void create(Road road) {
        roadDAO.save(road);

        // আবেদন জমার notification
        emailNotifier.sendApplicationReceived(
                road.getContact(),
                road.getName(),
                "রাস্তা সংস্কার আবেদন",
                String.valueOf(road.getId())
        );
    }

    public List<Road> getall() {
        return roadDAO.getall();
    }

    public List<Road> findByContact(String contact) {
        return roadDAO.findByContact(contact);
    }

    public Road getById(int id) {
        return roadDAO.getById(id);
    }

    public void updateStatus(int id, String status) {
        roadDAO.updateStatus(id, status);

        // Approve / Reject notification
        Road r = roadDAO.getById(id);
        if (r != null) {
            emailNotifier.sendStatusUpdate(
                    r.getContact(),
                    r.getName(),
                    "রাস্তা সংস্কার আবেদন",
                    String.valueOf(id),
                    status,
                    null
            );
        }
    }

    public void updateLocation(int id, Double lat, Double lng) {
        roadDAO.updateLocation(id, lat, lng);
    }
}
