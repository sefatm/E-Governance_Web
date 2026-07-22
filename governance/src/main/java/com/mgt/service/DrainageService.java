package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.DrainageDAO;
import com.mgt.model.Drainage;

@Service
public class DrainageService {

    @Autowired
    DrainageDAO drainageDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public void create(Drainage drainage) {
        drainageDAO.save(drainage);

        // আবেদন জমার notification
        emailNotifier.sendApplicationReceived(
                drainage.getContact(),
                drainage.getName(),
                "ড্রেনেজ অবকাঠামো আবেদন",
                String.valueOf(drainage.getId())
        );
    }

    public List<Drainage> getall() {
        return drainageDAO.getall();
    }

    public List<Drainage> findByContact(String contact) {
        return drainageDAO.findByContact(contact);
    }

    public Drainage getById(int id) {
        return drainageDAO.getById(id);
    }

    public void updateStatus(int id, String status) {
        drainageDAO.updateStatus(id, status);

        // Approve / Reject notification
        Drainage d = drainageDAO.getById(id);
        if (d != null) {
            emailNotifier.sendStatusUpdate(
                    d.getContact(),
                    d.getName(),
                    "ড্রেনেজ অবকাঠামো আবেদন",
                    String.valueOf(id),
                    status,
                    null
            );
        }
    }

    public void updateLocation(int id, Double lat, Double lng) {
        drainageDAO.updateLocation(id, lat, lng);
    }
}