package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.GarbageScheduleDAO;
import com.mgt.model.GarbageSchedule;

/**
 * GarbageSchedule — ward/area-based admin schedule।
 * Individual citizen contact নেই, তাই email notification প্রযোজ্য নয়।
 */
@Service
public class GarbageScheduleService {

    @Autowired
    GarbageScheduleDAO garbageDAO;

    public void create(GarbageSchedule garbage) {
        garbageDAO.save(garbage);
    }

    public List<GarbageSchedule> getall() {
        return garbageDAO.getall();
    }

    public GarbageSchedule getById(int id) {
        return garbageDAO.getById(id);
    }

    public void updateStatus(int id, String status) {
        garbageDAO.updateStatus(id, status);
    }

    public void update(GarbageSchedule garbage) {
        garbageDAO.update(garbage);
    }

    public void delete(int id) {
        garbageDAO.delete(id);
    }
}
