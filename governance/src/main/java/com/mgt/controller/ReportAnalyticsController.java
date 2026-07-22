package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.service.ReportAnalyticsService;

@RestController
@RequestMapping("/api/report")
public class ReportAnalyticsController {

    @Autowired
    private ReportAnalyticsService reportAnalyticsService;

    // Citizen Report
    
    @GetMapping("/citizens")
    public ResponseEntity<List<Map<String, Object>>> getCitizenReport() {
        return ResponseEntity.ok(reportAnalyticsService.getCitizenReport());
    }

    @GetMapping("/citizens/by-ward")
    public ResponseEntity<List<Map<String, Object>>> getCitizensByWard() {
        return ResponseEntity.ok(reportAnalyticsService.getCitizensByWard());
    }

    @GetMapping("/citizens/gender-distribution")
    public ResponseEntity<Map<String, Long>> getCitizenGenderDistribution() {
        return ResponseEntity.ok(reportAnalyticsService.getGenderDistribution());
    }

    // Service Report 

    @GetMapping("/services")
    public ResponseEntity<List<Map<String, Object>>> getServiceReport() {
        return ResponseEntity.ok(reportAnalyticsService.getServiceReport());
    }

    @GetMapping("/services/by-type")
    public ResponseEntity<List<Map<String, Object>>> getServicesByType() {
        return ResponseEntity.ok(reportAnalyticsService.getServicesByType());
    }

    @GetMapping("/services/by-status")
    public ResponseEntity<Map<String, Long>> getServicesByStatus() {
        return ResponseEntity.ok(reportAnalyticsService.getServicesByStatus());
    }

    // Monthly / Yearly Analytics 

    @GetMapping("/analytics/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyAnalytics(@RequestParam(defaultValue = "2026") int year) {
        return ResponseEntity.ok(reportAnalyticsService.getMonthlyAnalytics(year));
    }

    @GetMapping("/analytics/yearly")
    public ResponseEntity<List<Map<String, Object>>> getYearlyAnalytics(
            @RequestParam(defaultValue = "2020") int fromYear,
            @RequestParam(defaultValue = "2026") int toYear) {
        return ResponseEntity.ok(reportAnalyticsService.getYearlyAnalytics(fromYear, toYear));
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(reportAnalyticsService.getSummary());
    }

    // Tax Collection Report 

    @GetMapping("/tax")
    public ResponseEntity<List<Map<String, Object>>> getTaxCollectionReport() {
        return ResponseEntity.ok(reportAnalyticsService.getTaxCollectionReport());
    }

    @GetMapping("/tax/by-ward")
    public ResponseEntity<List<Map<String, Object>>> getTaxByWard() {
        return ResponseEntity.ok(reportAnalyticsService.getTaxByWard());
    }

    @GetMapping("/tax/monthly")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyTaxCollection(
            @RequestParam(defaultValue = "2026") int year) {
        return ResponseEntity.ok(reportAnalyticsService.getMonthlyTaxCollection(year));
    }
}
