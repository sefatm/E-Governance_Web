package com.mgt.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.ReportAnalyticsDAO;

@Service
public class ReportAnalyticsService {

    @Autowired
    private ReportAnalyticsDAO reportAnalyticsDAO;

    //Citizen Report 
    public List<Map<String, Object>> getCitizenReport() {
        return reportAnalyticsDAO.fetchAllCitizens();
    }

    public List<Map<String, Object>> getCitizensByWard() {
        return reportAnalyticsDAO.countCitizensByWard();
    }

    public Map<String, Long> getGenderDistribution() {
        return reportAnalyticsDAO.getCitizenGenderDistribution();
    }

    // Service Report 
    public List<Map<String, Object>> getServiceReport() {
        return reportAnalyticsDAO.fetchAllServiceRequests();
    }

    public List<Map<String, Object>> getServicesByType() {
        return reportAnalyticsDAO.countServicesByType();
    }

    public Map<String, Long> getServicesByStatus() {
        return reportAnalyticsDAO.countServicesByStatus();
    }

    // Monthly / Yearly Analytics
    public List<Map<String, Object>> getMonthlyAnalytics(int year) {
        Map<Integer, Long> raw = reportAnalyticsDAO.countServicesByMonth(year);

        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("month", monthNames[m - 1]);
            point.put("count", raw.getOrDefault(m, 0L));
            result.add(point);
        }
        return result;
    }

    public List<Map<String, Object>> getYearlyAnalytics(int fromYear, int toYear) {
        Map<Integer, Long> raw = reportAnalyticsDAO.countServicesByYear(fromYear, toYear);
        List<Map<String, Object>> result = new ArrayList<>();
        for (int y = fromYear; y <= toYear; y++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("year", String.valueOf(y));
            point.put("count", raw.getOrDefault(y, 0L));
            result.add(point);
        }
        return result;
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalCitizens",       reportAnalyticsDAO.countTotalCitizens());
        summary.put("totalServices",       reportAnalyticsDAO.countTotalServices());
        summary.put("pendingRequests",     reportAnalyticsDAO.countPendingRequests());
        summary.put("completedThisMonth",  reportAnalyticsDAO.countCompletedThisMonth());
        summary.put("totalRevenue",        reportAnalyticsDAO.sumTotalRevenue());
        summary.put("taxDueCount",         reportAnalyticsDAO.countTaxDue());
        return summary;
    }

    // Tax Collection Report 

    public List<Map<String, Object>> getTaxCollectionReport() {
        return reportAnalyticsDAO.fetchTaxPayments();
    }

    public List<Map<String, Object>> getTaxByWard() {
        return reportAnalyticsDAO.sumTaxByWard();
    }

    public List<Map<String, Object>> getMonthlyTaxCollection(int year) {
        Map<Integer, Double> raw = reportAnalyticsDAO.sumTaxByMonth(year);
        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("month", monthNames[m - 1]);
            point.put("amount", raw.getOrDefault(m, 0.0));
            result.add(point);
        }
        return result;
    }
}
