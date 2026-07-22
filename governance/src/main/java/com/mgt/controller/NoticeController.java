package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgt.model.Notice;
import com.mgt.service.NoticeService;

@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    // POST /api/notice/create
    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody Notice notice) {
        try {
            Notice saved = noticeService.create(notice);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // GET /api/notice/getall
    @GetMapping("/getall")
    public ResponseEntity<List<Notice>> getAll() {
        return ResponseEntity.ok(noticeService.getAll());
    }

    // GET /api/notice/active  — public-facing (no auth required)
    @GetMapping("/active")
    public ResponseEntity<List<Notice>> getActive() {
        return ResponseEntity.ok(noticeService.getActive());
    }

    // GET /api/notice/type/{type}  — filter by "Public" | "Emergency" | "Event" | "News"
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notice>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(noticeService.getByType(type));
    }

    // GET /api/notice/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        Notice notice = noticeService.getById(id);
        if (notice == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(notice);
    }

    // PUT /api/notice/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable int id, @RequestBody Notice notice) {
        try {
            Notice updated = noticeService.update(id, notice);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // PUT /api/notice/status/{id}
    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        noticeService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
    }

    // DELETE /api/notice/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        noticeService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Notice deleted successfully"));
    }
}
