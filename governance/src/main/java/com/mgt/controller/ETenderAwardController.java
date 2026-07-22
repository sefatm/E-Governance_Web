package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.ETenderAward;
import com.mgt.service.ETenderAwardService;

@RestController
@RequestMapping("/api/etender/award")
public class ETenderAwardController {

    @Autowired
    private ETenderAwardService service;

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<Object> award(@RequestBody ETenderAward award) {
        try {
            ETenderAward saved = service.awardTender(award);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ETenderAward>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        ETenderAward award = service.getById(id);
        if (award == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(award);
    }

    @GetMapping("/tender/{tenderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getByTenderId(@PathVariable int tenderId) {
        ETenderAward award = service.getByTenderId(tenderId);
        if (award == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(award);
    }
}
