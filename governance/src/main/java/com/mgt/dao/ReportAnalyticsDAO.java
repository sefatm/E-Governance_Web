package com.mgt.dao;

import java.time.LocalDate;
import java.util.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;


@Repository
public class ReportAnalyticsDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchAllCitizens() {
        String sql = """
            SELECT id,
                   full_name                                                              AS name,
                   TIMESTAMPDIFF(YEAR, STR_TO_DATE(date_of_birth, '%Y-%m-%d'), CURDATE()) AS age,
                   district                                                               AS ward,
                   gender,
                   status,
                   DATE(created_at)                                                       AS createdDate
            FROM citizen_certificate
            ORDER BY id DESC
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "id","name","age","ward","gender","status","createdDate");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> countCitizensByWard() {
        String sql = """
            SELECT district AS ward, COUNT(*) AS count
            FROM citizen_certificate
            WHERE district IS NOT NULL
            GROUP BY district
            ORDER BY count DESC
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(), "ward","count");
    }

    public Map<String, Long> getCitizenGenderDistribution() {
        String sql = """
            SELECT gender, COUNT(*) AS cnt
            FROM citizen_certificate
            WHERE gender IS NOT NULL AND gender != ''
            GROUP BY gender
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return result;
    }

    public long countTotalCitizens() {
        return ((Number) entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM citizen_certificate").getSingleResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchAllServiceRequests() {
        String sql = """
            SELECT applicant_name AS citizenName, 'Birth/Death Certificate' AS serviceType,
                   status, DATE(created_at) AS appliedDate
            FROM birth_death_certificate
            UNION ALL
            SELECT ownername,     'Trade License',          status, date
            FROM trade_license_apply
            UNION ALL
            SELECT name,          'Water Connection',
                   CASE
                     WHEN status LIKE '{%' THEN JSON_UNQUOTE(JSON_EXTRACT(status, '$.status'))
                     ELSE status
                   END,
                   start_date
            FROM water_connection
            UNION ALL
            SELECT full_name,     'Citizen Certificate',   status, DATE(created_at)
            FROM citizen_certificate
            UNION ALL
            SELECT name,          'Passport Application',  status, application_date
            FROM passport_apply
            ORDER BY appliedDate DESC
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "citizenName","serviceType","status","appliedDate");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> countServicesByType() {
        String sql = """
            SELECT serviceType, COUNT(*) AS count FROM (
              SELECT 'Birth/Death Certificate' AS serviceType FROM birth_death_certificate
              UNION ALL SELECT 'Trade License'              FROM trade_license_apply
              UNION ALL SELECT 'Water Connection'           FROM water_connection
              UNION ALL SELECT 'Citizen Certificate'        FROM citizen_certificate
              UNION ALL SELECT 'Passport Application'       FROM passport_apply
            ) t GROUP BY serviceType ORDER BY count DESC
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(), "serviceType","count");
    }

    public Map<String, Long> countServicesByStatus() {
        String sql = """
            SELECT status, COUNT(*) FROM (
              SELECT status FROM birth_death_certificate
              UNION ALL SELECT status FROM trade_license_apply
              UNION ALL SELECT status FROM citizen_certificate
              UNION ALL SELECT status FROM passport_apply
              UNION ALL
              SELECT
                CASE
                  WHEN status LIKE '{%' THEN JSON_UNQUOTE(JSON_EXTRACT(status, '$.status'))
                  ELSE status
                END AS status
              FROM water_connection
            ) t WHERE status IS NOT NULL GROUP BY status
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql).getResultList();
        // Normalize and merge: treat case-insensitively (e.g. "APPROVED" == "Approved")
        Map<String, Long> normalized = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String raw = String.valueOf(row[0]);
            // Capitalize first letter, lowercase rest for consistent labels
            String key = raw.isEmpty() ? raw
                : Character.toUpperCase(raw.charAt(0)) + raw.substring(1).toLowerCase();
            // Special case: "In progress" → "In Progress"
            if (key.equalsIgnoreCase("in progress")) key = "In Progress";
            long count = ((Number) row[1]).longValue();
            normalized.merge(key, count, Long::sum);
        }
        return normalized;
    }

    public long countTotalServices() {
        String sql = """
            SELECT SUM(cnt) FROM (
              SELECT COUNT(*) AS cnt FROM birth_death_certificate
              UNION ALL SELECT COUNT(*) FROM trade_license_apply
              UNION ALL SELECT COUNT(*) FROM water_connection
              UNION ALL SELECT COUNT(*) FROM citizen_certificate
              UNION ALL SELECT COUNT(*) FROM passport_apply
            ) t
            """;
        Object r = entityManager.createNativeQuery(sql).getSingleResult();
        return r == null ? 0L : ((Number) r).longValue();
    }

    public long countPendingRequests() {
        String sql = """
            SELECT COUNT(*) FROM (
              SELECT status FROM birth_death_certificate  WHERE LOWER(status) = 'pending'
              UNION ALL SELECT status FROM trade_license_apply WHERE LOWER(status) = 'pending'
              UNION ALL
              SELECT status FROM water_connection
              WHERE LOWER(
                CASE
                  WHEN status LIKE '{%' THEN JSON_UNQUOTE(JSON_EXTRACT(status, '$.status'))
                  ELSE status
                END
              ) = 'pending'
              UNION ALL SELECT status FROM citizen_certificate  WHERE LOWER(status) = 'pending'
              UNION ALL SELECT status FROM passport_apply       WHERE LOWER(status) = 'pending'
            ) t
            """;
        return ((Number) entityManager.createNativeQuery(sql).getSingleResult()).longValue();
    }

    public long countCompletedThisMonth() {
        int month = LocalDate.now().getMonthValue();
        int year  = LocalDate.now().getYear();
        String sql = """
            SELECT COUNT(*) FROM (
              SELECT created_at AS d FROM birth_death_certificate WHERE LOWER(status) = 'approved'
              UNION ALL SELECT date       FROM trade_license_apply  WHERE LOWER(status) = 'approved'
              UNION ALL
              SELECT start_date FROM water_connection
              WHERE LOWER(
                CASE
                  WHEN status LIKE '{%' THEN JSON_UNQUOTE(JSON_EXTRACT(status, '$.status'))
                  ELSE status
                END
              ) = 'approved'
              UNION ALL SELECT created_at        FROM citizen_certificate WHERE LOWER(status) = 'approved'
              UNION ALL SELECT application_date  FROM passport_apply      WHERE LOWER(status) = 'approved'
            ) t WHERE MONTH(d) = :month AND YEAR(d) = :year
            """;
        return ((Number) entityManager.createNativeQuery(sql)
                .setParameter("month", month)
                .setParameter("year", year)
                .getSingleResult()).longValue();
    }

    public double sumTotalRevenue() {
        // FIX: PaymentService.confirm() sets status = "Completed" (not "success")
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payment_transaction WHERE status = 'Completed'";
        Object result = entityManager.createNativeQuery(sql).getSingleResult();
        return result == null ? 0.0 : ((Number) result).doubleValue();
    }

    public long countTaxDue() {

        Object result = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM holding_new_registration WHERE UPPER(status)='APPROVED'")
                .getSingleResult();

        return ((Number) result).longValue();
    }

    // Monthly / Yearly Analytics 

    public Map<Integer, Long> countServicesByMonth(int year) {
        String sql = """
            SELECT MONTH(d) AS m, COUNT(*) AS cnt FROM (
              SELECT created_at AS d FROM birth_death_certificate WHERE YEAR(created_at) = :year
              UNION ALL SELECT date             FROM trade_license_apply  WHERE YEAR(date)             = :year
              UNION ALL SELECT start_date       FROM water_connection      WHERE YEAR(start_date)       = :year
              UNION ALL SELECT created_at        FROM citizen_certificate   WHERE YEAR(created_at)       = :year
              UNION ALL SELECT application_date FROM passport_apply        WHERE YEAR(application_date) = :year
            ) t GROUP BY MONTH(d)
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("year", year).getResultList();
        Map<Integer, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return result;
    }

    public Map<Integer, Long> countServicesByYear(int from, int to) {
        String sql = """
            SELECT YEAR(d) AS y, COUNT(*) AS cnt FROM (
              SELECT created_at      AS d FROM birth_death_certificate
              UNION ALL SELECT date             FROM trade_license_apply
              UNION ALL SELECT start_date       FROM water_connection
              UNION ALL SELECT created_at        FROM citizen_certificate
              UNION ALL SELECT application_date FROM passport_apply
            ) t WHERE YEAR(d) BETWEEN :from AND :to GROUP BY YEAR(d)
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("from", from).setParameter("to", to).getResultList();
        Map<Integer, Long> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }
        return result;
    }

    // Tax Collection 

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchTaxPayments() {
        String sql = """
            SELECT id,
                   owner_name          AS citizenName,
                   holding_no          AS ward,
                   amount,
                   payment_date        AS paymentDate,
                   status
            FROM tax_payment
            ORDER BY created_at DESC
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "id", "citizenName", "ward", "amount", "paymentDate", "status");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> sumTaxByWard() {
        String sql = """
            SELECT COALESCE(tp.holding_no, 'Unknown') AS ward,
                   SUM(tp.amount) AS total
            FROM tax_payment tp
            WHERE tp.status = 'Paid'
            GROUP BY tp.holding_no
            ORDER BY total DESC
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(), "ward", "total");
    }

    public Map<Integer, Double> sumTaxByMonth(int year) {
        String sql = """
            SELECT MONTH(payment_date), SUM(amount)
            FROM tax_payment
            WHERE status = 'Paid'
              AND YEAR(payment_date) = :year
            GROUP BY MONTH(payment_date)
            """;
        List<Object[]> rows = entityManager.createNativeQuery(sql)
                .setParameter("year", year).getResultList();
        Map<Integer, Double> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }
        return result;
    }

    // Utility 

    private List<Map<String, Object>> toMapList(List<Object[]> rows, String... columns) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (int i = 0; i < columns.length && i < row.length; i++) {
                map.put(columns[i], row[i]);
            }
            result.add(map);
        }
        return result;
    }
}
