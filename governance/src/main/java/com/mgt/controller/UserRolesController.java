package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.AppUser;
import com.mgt.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserRolesController {

    @Autowired
    UserService userService;

    // GET /api/users/getall 
    @GetMapping("/getall")
    public List<AppUser> getAll() {
        return userService.getAll();
    }

    // GET /api/users/pending
    @GetMapping("/pending")
    public List<AppUser> getPending() {
        return userService.getPending();
    }

    // GET /api/users/{id} 
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        AppUser user = userService.getById(id);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(user);
    }

    // PUT /api/users/update-role/{id} 
    @PutMapping("/update-role/{id}")
    public ResponseEntity<Object> updateRole(@PathVariable int id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || role.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Role is required."));
        userService.updateRole(id, role);
        return ResponseEntity.ok(Map.of("message", "Role updated successfully."));
    }

    //  PUT /api/users/update-status/{id}
    @PutMapping("/update-status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Status is required."));
        userService.updateStatus(id, status);
        return ResponseEntity.ok(Map.of("message", "Status updated successfully."));
    }

    // PUT /api/users/approve/{id}
    @PutMapping("/approve/{id}")
    public ResponseEntity<Object> approve(@PathVariable int id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || role.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message", "Role is required."));

        AppUser user = userService.getById(id);
        if (user == null)
            return ResponseEntity.notFound().build();
        if (!"Pending".equalsIgnoreCase(user.getStatus()))
            return ResponseEntity.badRequest().body(Map.of("message", "User is not in Pending status."));

        userService.approve(id, role);
        return ResponseEntity.ok(Map.of("message", "User approved successfully."));
    }

    // PUT /api/users/reject/{id}
    @PutMapping("/reject/{id}")
    public ResponseEntity<Object> reject(@PathVariable int id) {
        AppUser user = userService.getById(id);
        if (user == null)
            return ResponseEntity.notFound().build();
        if (!"Pending".equalsIgnoreCase(user.getStatus()))
            return ResponseEntity.badRequest().body(Map.of("message", "User is not in Pending status."));

        userService.updateStatus(id, "Inactive");
        return ResponseEntity.ok(Map.of("message", "User rejected."));
    }

    // DELETE /api/users/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        if (userService.getById(id) == null)
            return ResponseEntity.notFound().build();
        userService.delete(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully."));
    }
}
