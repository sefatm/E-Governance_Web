package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.StreetLightDAO;
import com.mgt.model.StreetLight;

@Service
public class StreetLightService {

    @Autowired
    StreetLightDAO lightDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public void create(StreetLight light) {
        lightDAO.save(light);

        // আবেদন জমার notification
        emailNotifier.sendApplicationReceived(
                light.getContact(),
                light.getName(),
                "স্ট্রিট লাইট সমস্যা আবেদন",
                String.valueOf(light.getId())
        );
    }

    public List<StreetLight> getall() {
        return lightDAO.getall();
    }

    public List<StreetLight> findByContact(String contact) {
        return lightDAO.findByContact(contact);
    }

    public void updateStatus(int id, String status) {
        lightDAO.updateStatus(id, status);

        // Approve/Reject notification — getAll() থেকে id match করে fetch
        lightDAO.getall().stream()
                .filter(l -> l.getId() == id)
                .findFirst()
                .ifPresent(l -> emailNotifier.sendStatusUpdate(
                        l.getContact(),
                        l.getName(),
                        "স্ট্রিট লাইট সমস্যা আবেদন",
                        String.valueOf(id),
                        status,
                        null
                ));
    }

    public void updateLocation(int id, Double lat, Double lng) {
        lightDAO.updateLocation(id, lat, lng);
    }
}