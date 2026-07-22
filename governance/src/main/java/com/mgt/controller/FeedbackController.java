package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgt.model.CitizenFeedback;
import com.mgt.service.FeedbackService;

@RestController
@RequestMapping(value = "/api/feedback")
public class FeedbackController {

    @Autowired
    FeedbackService feedbackService;

    // POST /api/feedback/submit
    @PostMapping("/submit")
    public CitizenFeedback submit(@RequestBody CitizenFeedback fb) {
        return feedbackService.submit(fb);
    }

    // GET /api/feedback/getall
    @GetMapping("/getall")
    public List<CitizenFeedback> getAll() {
        return feedbackService.getAll();
    }

    // GET /api/feedback/{id}
    @GetMapping("/{id}")
    public CitizenFeedback getById(@PathVariable int id) {
        return feedbackService.getById(id);
    }

    // GET /api/feedback/status/{status}
    @GetMapping("/status/{status}")
    public List<CitizenFeedback> getByStatus(@PathVariable String status) {
        return feedbackService.getByStatus(status);
    }

    // GET /api/feedback/category/{category}
    @GetMapping("/category/{category}")
    public List<CitizenFeedback> getByCategory(@PathVariable String category) {
        return feedbackService.getByCategory(category);
    }

    // PUT /api/feedback/reply/{id}
    @PutMapping("/reply/{id}")
    public CitizenFeedback reply(@PathVariable int id, @RequestBody Map<String, String> body) {
        String reply = body.get("reply");
        if (reply == null) reply = body.get("adminReply");
        return feedbackService.reply(id, reply, body.get("status"));
    }

    // PUT /api/feedback/status/{id}
    @PutMapping("/status/{id}")
    public CitizenFeedback updateStatus(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        return feedbackService.updateStatus(id, body.get("status"));
    }

    // DELETE /api/feedback/{id}
    @DeleteMapping("/{id}")
    public void delete(@PathVariable int id) {
        feedbackService.delete(id);
    }

    // GET /api/feedback/summary
    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        return feedbackService.getSummary();
    }
}
