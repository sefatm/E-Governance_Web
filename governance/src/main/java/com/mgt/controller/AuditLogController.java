package com.mgt.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.mgt.model.AuditLog;
import com.mgt.service.AuditLogService;

@RestController
@RequestMapping(value = "/api/auditlog")
public class AuditLogController {

    @Autowired
    AuditLogService auditLogService;

    // GET /api/auditlog/getall
    @GetMapping("/getall")
    public List<AuditLog> getAll() {
        return auditLogService.getAll();
    }

    // GET /api/auditlog/user/{username}
    @GetMapping("/user/{username}")
    public List<AuditLog> getByUsername(@PathVariable String username) {
        return auditLogService.getByUsername(username);
    }

    // GET /api/auditlog/module/{module}
    @GetMapping("/module/{module}")
    public List<AuditLog> getByModule(@PathVariable String module) {
        return auditLogService.getByModule(module);
    }

    // DELETE /api/auditlog/clear
    @DeleteMapping("/clear")
    public void clearAll() {
        auditLogService.clearAll();
    }
}
