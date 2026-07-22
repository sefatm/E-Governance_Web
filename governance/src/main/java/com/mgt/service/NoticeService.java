package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.NoticeDAO;
import com.mgt.model.Notice;

@Service
public class NoticeService {

    @Autowired
    private NoticeDAO noticeDAO;

    public Notice create(Notice notice) {
        noticeDAO.save(notice);
        return notice;
    }

    public List<Notice> getAll() {
        return noticeDAO.getAll();
    }

    public List<Notice> getByType(String type) {
        return noticeDAO.getByType(type);
    }

    public List<Notice> getActive() {
        return noticeDAO.getActive();
    }

    public Notice getById(int id) {
        return noticeDAO.getById(id);
    }

    public Notice update(int id, Notice notice) {
        Notice existing = noticeDAO.getById(id);
        if (existing == null) {
            throw new RuntimeException("Notice not found with id: " + id);
        }
        notice.setId(id);
        notice.setCreatedAt(existing.getCreatedAt());
        noticeDAO.update(notice);
        return notice;
    }

    public void updateStatus(int id, String status) {
        noticeDAO.updateStatus(id, status);
    }

    public void delete(int id) {
        noticeDAO.delete(id);
    }
}
