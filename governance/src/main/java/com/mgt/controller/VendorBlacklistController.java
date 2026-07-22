package com.mgt.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.VendorBlacklist;
import com.mgt.service.VendorBlacklistService;

/**
 * Vendor Blacklist API
 *
 * POST   /api/etender/blacklist/add            → vendor blacklist এ add করো
 * GET    /api/etender/blacklist/getall          → সব blacklisted vendors
 * GET    /api/etender/blacklist/active          → শুধু active blocked vendors
 * PUT    /api/etender/blacklist/unblock/{id}    → vendor কে unblock করো
 * DELETE /api/etender/blacklist/delete/{id}     → permanently delete
 * GET    /api/etender/blacklist/check           → bid submit এর আগে check করো
 */
@RestController
@RequestMapping("/api/etender/blacklist")
public class VendorBlacklistController {

    @Autowired
    private VendorBlacklistService service;

    // ─── Vendor কে blacklist এ add করো ─────────────────────────────────────
    @PostMapping("/add")
    public ResponseEntity<Object> add(@RequestBody VendorBlacklist vendor) {
        if (vendor.getNid() == null && vendor.getEmail() == null && vendor.getMobile() == null) {
            return ResponseEntity.badRequest()
                .body(Map.of("message", "NID, Email অথবা Mobile — অন্তত একটা দিতে হবে।"));
        }
        vendor.setBlacklistedAt(LocalDateTime.now());
        vendor.setActive(true);
        VendorBlacklist saved = service.blacklist(vendor);
        return ResponseEntity.ok(saved);
    }

    // ─── সব blacklisted vendors ──────────────────────────────────────────────
    @GetMapping("/getall")
    public ResponseEntity<List<VendorBlacklist>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // ─── শুধু active blocked vendors ─────────────────────────────────────────
    @GetMapping("/active")
    public ResponseEntity<List<VendorBlacklist>> getActive() {
        return ResponseEntity.ok(service.getActive());
    }

    // ─── Vendor কে unblock করো ───────────────────────────────────────────────
    @PutMapping("/unblock/{id}")
    public ResponseEntity<Object> unblock(@PathVariable int id) {
        VendorBlacklist v = service.getById(id);
        if (v == null) return ResponseEntity.notFound().build();
        service.unblock(id);
        return ResponseEntity.ok(Map.of("message", v.getVendorName() + " সফলভাবে unblock হয়েছে।"));
    }

    // ─── Permanently delete ───────────────────────────────────────────────────
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Blacklist entry মুছে ফেলা হয়েছে।"));
    }

    /**
     * Bid submit করার আগে Angular frontend এ call করো
     * GET /api/etender/blacklist/check?nid=X&email=Y&mobile=Z
     * Response: { "blacklisted": true/false }
     */
    @GetMapping("/check")
    public ResponseEntity<Object> check(
            @RequestParam(required = false) String nid,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobile) {
        boolean blocked = service.isBlacklisted(nid, email, mobile);
        return ResponseEntity.ok(Map.of(
            "blacklisted", blocked,
            "message", blocked
                ? "এই vendor blacklisted। bid জমা দেওয়া যাবে না।"
                : "Vendor eligible।"
        ));
    }
}
