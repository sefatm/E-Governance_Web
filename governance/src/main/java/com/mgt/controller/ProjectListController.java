package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.ProjectList;
import com.mgt.service.ProjectListService;

@RestController
@RequestMapping(value = "/api/project-list")
public class ProjectListController {

    @Autowired ProjectListService projectService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody ProjectList project) {
        try {
            projectService.create(project);
            return ResponseEntity.ok(Map.of("message", "Project created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<ProjectList> getall() {
        return projectService.getall();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        ProjectList p = projectService.getById(id);
        if (p == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(p);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        projectService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/progress/{id}")
    public ResponseEntity<Object> updateProgress(@PathVariable int id, @RequestBody Map<String, Integer> body) {
        projectService.updateProgress(id, body.get("progress"));
        return ResponseEntity.ok(Map.of("message", "Progress updated"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable int id, @RequestBody ProjectList project) {
        project.setId(id);
        projectService.update(project);
        return ResponseEntity.ok(Map.of("message", "Project updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        projectService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
