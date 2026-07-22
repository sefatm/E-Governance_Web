package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.ETenderNotice;
import com.mgt.service.ETenderNoticeService;

@RestController
@RequestMapping("/api/etender/notice")
public class ETenderNoticeController {

    @Autowired
    private ETenderNoticeService service;

    // Admin only — Tender তৈরি
    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<Object> create(@RequestBody ETenderNotice notice) {
        try {
            return ResponseEntity.ok(service.create(notice));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Public — সবাই দেখতে পারবে
    @GetMapping("/getall")
    public ResponseEntity<List<ETenderNotice>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/open")
    public ResponseEntity<List<ETenderNotice>> getOpen() {
        return ResponseEntity.ok(service.getOpen());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        ETenderNotice notice = service.getById(id);
        if (notice == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(notice);
    }

    // Admin only — status পরিবর্তন
    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        service.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
    }

    // Admin only — update
    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<Object> update(@PathVariable int id, @RequestBody ETenderNotice notice) {
        try {
            return ResponseEntity.ok(service.update(id, notice));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Admin only — delete, bid check যোগ
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer')")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(Map.of("message", "Tender notice deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Admin only — manual close expired
    @PostMapping("/close-expired")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<Object> closeExpired() {
        int count = service.manualCloseExpired();
        return ResponseEntity.ok(Map.of(
            "message", count + " টি মেয়াদোত্তীর্ণ Tender বন্ধ করা হয়েছে।",
            "closedCount", count
        ));
    }
}
