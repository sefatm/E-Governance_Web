package com.mgt.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "system_setting")
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "setting_key", unique = true)
    private String settingKey;

    @Column(name = "setting_val")
    private String settingVal;

    private String label;
    private String category = "General";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // ── Getters & Setters ──────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSettingKey() { return settingKey; }
    public void setSettingKey(String settingKey) { this.settingKey = settingKey; }

    public String getSettingVal() { return settingVal; }
    public void setSettingVal(String settingVal) { this.settingVal = settingVal; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
