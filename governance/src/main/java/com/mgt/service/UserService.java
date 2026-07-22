package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.mgt.dao.UserDAO;
import com.mgt.model.AppUser;

@Service
public class UserService {

    @Autowired
    UserDAO userDAO;

    @Autowired
    PasswordEncoder passwordEncoder;

    // Register
    public String register(AppUser user) {
        if (userDAO.findByEmail(user.getEmail()) != null) {
            return "Email already registered.";
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("Citizen");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus("Pending");
        userDAO.save(user);
        return null;
    }

    // Login
    public AppUser login(String email, String password) {
        AppUser user = userDAO.findByEmail(email);
        if (user == null) return null;
        if (!passwordEncoder.matches(password, user.getPassword())) return null;
        if ("Inactive".equalsIgnoreCase(user.getStatus())) return null;
        if ("Pending".equalsIgnoreCase(user.getStatus()))  return null;

        return user;
    }

    // Get All
    public List<AppUser> getAll() {
        return userDAO.getAll();
    }

    // Get Pending
    public List<AppUser> getPending() {
        return userDAO.getPending();
    }

    // Find By Email 
    public AppUser findByEmail(String email) {
        return userDAO.findByEmail(email);
    }

    // Get By ID
    public AppUser getById(int id) {
        return userDAO.getById(id);
    }

    // Update Role
    public void updateRole(int id, String role) {
        userDAO.updateRole(id, role);
    }

    // Update Status
    public void updateStatus(int id, String status) {
        userDAO.updateStatus(id, status);
    }

    // Approve 
    public void approve(int id, String role) {
        userDAO.approve(id, role);
    }

    // Delete
    public void delete(int id) {
        userDAO.delete(id);
    }

    // Verify Password
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // Change Password (admin reset)
    public void changePassword(int id, String newRawPassword) {
        String hashed = passwordEncoder.encode(newRawPassword);
        userDAO.changePassword(id, hashed);
    }

    // Update Profile
    public void updateProfile(int id, String name, String email) {
        userDAO.updateProfile(id, name, email);
    }

    // Update Photo
    public void updatePhoto(int id, String photoUrl) {
        userDAO.updatePhoto(id, photoUrl);
    }
}
