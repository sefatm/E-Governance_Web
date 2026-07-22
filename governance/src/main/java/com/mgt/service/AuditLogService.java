package com.mgt.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.AuditLogDAO;
import com.mgt.model.AuditLog;

@Service
public class AuditLogService {

    @Autowired
    AuditLogDAO auditLogDAO;

    public AuditLog log(String username, String userRole,
            String action, String module,
            String details, String ipAddress) {
    	AuditLog log = new AuditLog();

    	if (username == null || username.trim().isEmpty()) {
    		username = "SYSTEM";
    	}

    	if (userRole == null || userRole.trim().isEmpty()) {
    		userRole = "UNKNOWN";
    	}

    	log.setUsername(username);
    	log.setUserRole(userRole);
    	log.setAction(action);
    	log.setModule(module);
    	log.setDetails(details);
    	log.setIpAddress(ipAddress);
    	log.setStatus("Success");
    	log.setCreatedAt(LocalDateTime.now());

    	return auditLogDAO.save(log);
    }

    public List<AuditLog> getAll() {
        return auditLogDAO.getAll();
    }

    public List<AuditLog> getByUsername(String username) {
        return auditLogDAO.getByUsername(username);
    }

    public List<AuditLog> getByModule(String module) {
        return auditLogDAO.getByModule(module);
    }

    public void clearAll() {
        auditLogDAO.deleteAll();
    }
}
