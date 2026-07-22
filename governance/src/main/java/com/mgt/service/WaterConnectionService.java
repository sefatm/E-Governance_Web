package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.WaterConnectionDAO;
import com.mgt.model.WaterConnection;

@Service
public class WaterConnectionService {

    @Autowired
    WaterConnectionDAO waterDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public void create(WaterConnection water) {
        waterDAO.save(water);

        // email থাকলে email-এ, না থাকলে notification যাবে না
        emailNotifier.sendApplicationReceived(
                water.getEmail(),
                water.getName(),
                "পানি সংযোগ আবেদন",
                String.valueOf(water.getId())
        );
    }

    public List<WaterConnection> getall() {
        return waterDAO.getall();
    }

    public WaterConnection getById(int id) {
        return waterDAO.getById(id);
    }

    public void updateStatus(int id, String status) {
        waterDAO.updateStatus(id, status);

        // Approve / Reject notification
        WaterConnection w = waterDAO.getById(id);
        if (w != null) {
            emailNotifier.sendStatusUpdate(
                    w.getEmail(),
                    w.getName(),
                    "পানি সংযোগ আবেদন",
                    String.valueOf(id),
                    status,
                    null
            );
        }
    }

    public void update(WaterConnection water) {
        waterDAO.update(water);
    }

    public void delete(int id) {
        waterDAO.delete(id);
    }
}
