package com.mgt.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.SystemSettingDAO;
import com.mgt.model.SystemSetting;

@Service
public class SystemSettingService {

    @Autowired
    SystemSettingDAO settingDAO;

    public List<SystemSetting> getAll() {
        return settingDAO.getAll();
    }

    public List<SystemSetting> getByCategory(String category) {
        return settingDAO.getByCategory(category);
    }

    public SystemSetting getById(int id) {
        return settingDAO.getById(id);
    }

    public SystemSetting updateValue(int id, String newVal) {
        SystemSetting s = settingDAO.getById(id);
        if (s == null) throw new RuntimeException("Setting not found.");
        s.setSettingVal(newVal);
        s.setUpdatedAt(LocalDateTime.now());
        return settingDAO.update(s);
    }

    public SystemSetting updateByKey(String key, String newVal) {
        SystemSetting s = settingDAO.getByKey(key);
        if (s == null) throw new RuntimeException("Setting key not found: " + key);
        s.setSettingVal(newVal);
        s.setUpdatedAt(LocalDateTime.now());
        return settingDAO.update(s);
    }
}
