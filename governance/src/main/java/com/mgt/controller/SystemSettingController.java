package com.mgt.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.mgt.model.SystemSetting;
import com.mgt.service.SystemSettingService;

@RestController
@RequestMapping(value = "/api/settings")
public class SystemSettingController {

    @Autowired
    SystemSettingService settingService;

    // GET /api/settings/getall
    @GetMapping("/getall")
    public List<SystemSetting> getAll() {
        return settingService.getAll();
    }

    // GET /api/settings/category/{category}
    @GetMapping("/category/{category}")
    public List<SystemSetting> getByCategory(@PathVariable String category) {
        return settingService.getByCategory(category);
    }

    // GET /api/settings/{id}
    @GetMapping("/{id}")
    public SystemSetting getById(@PathVariable int id) {
        return settingService.getById(id);
    }

    // PUT /api/settings/update/{id}
    @PutMapping("/update/{id}")
    public SystemSetting update(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        return settingService.updateValue(id, body.get("value"));
    }

    // PUT /api/settings/updatekey
    @PutMapping("/updatekey")
    public SystemSetting updateByKey(@RequestBody Map<String, String> body) {
        return settingService.updateByKey(body.get("key"), body.get("value"));
    }
}
