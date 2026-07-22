package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.PassportDAO;
import com.mgt.model.PassportApply;

@Service
public class PassportService {

    @Autowired
    PassportDAO passportDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public void create(PassportApply passport) {
        passportDAO.save(passport);

        // আবেদন জমার notification
        String contact = passport.getEmail() != null ? passport.getEmail() : passport.getContact();
        emailNotifier.sendApplicationReceived(
                contact,
                passport.getFullName(),
                "পাসপোর্ট আবেদন",
                null
        );
    }

    public List<PassportApply> getall() {
        return passportDAO.getall();
    }

    public List<PassportApply> findByContact(String mobile) { return passportDAO.findByContact(mobile); }

    public void updateStatus(int id, String status) {
        passportDAO.updateStatus(id, status);
        sendStatusNotification(id, status, null);
    }

    public void approve(int id) {
        passportDAO.approve(id);
        sendStatusNotification(id, "Approved", null);
    }

    public void reject(int id, String reason) {
        passportDAO.reject(id, reason);
        sendStatusNotification(id, "Rejected", reason);
    }

    public void update(int id, PassportApply passport) {
        passport.setId(id);
        passportDAO.update(passport);
    }

    public void updateFiles(int id, String photoUrl, String nidFileUrl, String birthFileUrl) {
        passportDAO.updateFiles(id, photoUrl, nidFileUrl, birthFileUrl);
    }

    public void delete(int id) {
        passportDAO.delete(id);
    }

    private void sendStatusNotification(int id, String status, String reason) {
        // PassportDAO needs a getById — fetch by id if available
        // Using a lightweight lookup; if passportDAO lacks getById, skip silently
        try {
            List<PassportApply> all = passportDAO.getall();
            all.stream()
               .filter(p -> p.getId() == id)
               .findFirst()
               .ifPresent(passport -> {
                   String contact = passport.getEmail() != null ? passport.getEmail() : passport.getContact();
                   emailNotifier.sendStatusUpdate(
                           contact,
                           passport.getFullName(),
                           "পাসপোর্ট আবেদন",
                           String.valueOf(passport.getId()),
                           status,
                           reason
                   );
               });
        } catch (Exception ignored) {
            // notification failure should never break the main flow
        }
    }
}
