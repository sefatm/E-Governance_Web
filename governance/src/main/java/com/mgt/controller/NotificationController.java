package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.NotificationMessage;
import com.mgt.service.NotificationService;


@RestController
@RequestMapping(value = "/api/notification")
public class NotificationController {

    @Autowired NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<Object> send(@RequestBody NotificationMessage msg) {
        try {
            NotificationMessage saved = notificationService.send(msg);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<NotificationMessage> getAll() {
        return notificationService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        NotificationMessage n = notificationService.getById(id);
        if (n == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(n);
    }

    @GetMapping("/type/{type}")
    public List<NotificationMessage> getByType(@PathVariable String type) {
        return notificationService.getByType(type);
    }

    @GetMapping("/tag/{tag}")
    public List<NotificationMessage> getByTag(@PathVariable String tag) {
        return notificationService.getByTag(tag);
    }

    @GetMapping("/summary")
    public ResponseEntity<Object> getSummary() {
        return ResponseEntity.ok(notificationService.getSummary());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        notificationService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
