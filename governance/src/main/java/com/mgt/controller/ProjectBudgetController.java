package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.ProjectBudget;
import com.mgt.service.ProjectBudgetService;


@RestController
@RequestMapping(value = "/api/project-budget")
public class ProjectBudgetController {

    @Autowired ProjectBudgetService budgetService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody ProjectBudget budget) {
        try {
            budgetService.create(budget);
            return ResponseEntity.ok(Map.of("message", "Budget created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<ProjectBudget> getall() {
        return budgetService.getall();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        ProjectBudget b = budgetService.getById(id);
        if (b == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(b);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        budgetService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Object> update(@PathVariable int id, @RequestBody ProjectBudget budget) {
        budget.setId(id);
        budgetService.update(budget);
        return ResponseEntity.ok(Map.of("message", "Budget updated"));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        budgetService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
