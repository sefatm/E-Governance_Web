package com.mgt.dao;

import java.util.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Repository;

@Repository
@Transactional
public class GisMapDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // Holding Locations — reads latitude/longitude (the columns updateLocation() writes to)
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchHoldingLocations() {
        String sql = """
            SELECT id              AS holdingId,
                   applicant_name  AS ownerName,
                   holding_no      AS holdingNo,
                   ward,
                   area,
                   status,
                   latitude AS lat, longitude AS lng
            FROM holding_new_registration
            WHERE latitude IS NOT NULL AND longitude IS NOT NULL
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "holdingId", "ownerName", "holdingNo", "ward", "area", "status", "lat", "lng");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchHoldingsByWard(int wardNo) {
        String sql = """
            SELECT id               AS holdingId,
                   applicant_name   AS ownerName,
                   holding_no       AS holdingNo,
                   ward, area, status,
                   latitude AS lat, longitude AS lng
            FROM holding_new_registration
            WHERE ward = :ward AND latitude IS NOT NULL AND longitude IS NOT NULL
            """;
        return toMapList(
            entityManager.createNativeQuery(sql).setParameter("ward", wardNo).getResultList(),
            "holdingId", "ownerName", "holdingNo", "ward", "area", "status", "lat", "lng");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchHoldingsByTaxStatus(String status) {
        String sql = """
            SELECT id               AS holdingId,
                   applicant_name   AS ownerName,
                   holding_no       AS holdingNo,
                   ward, area, status,
                   latitude AS lat, longitude AS lng
            FROM holding_new_registration
            WHERE status = :status AND latitude IS NOT NULL AND longitude IS NOT NULL
            """;
        return toMapList(
            entityManager.createNativeQuery(sql).setParameter("status", status).getResultList(),
            "holdingId", "ownerName", "holdingNo", "ward", "area", "status", "lat", "lng");
    }

    // All holdings with location info for map dashboard
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchAllHoldingsForMap() {
        String sql = """
            SELECT id               AS holdingId,
                   applicant_name   AS ownerName,
                   holding_no       AS holdingNo,
                   ward, area, status,
                   latitude AS lat, longitude AS lng,
                   CASE WHEN latitude IS NOT NULL THEN 1 ELSE 0 END AS hasPinned
            FROM holding_new_registration
            ORDER BY ward, id
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
            "holdingId", "ownerName", "holdingNo", "ward", "area", "status", "lat", "lng", "hasPinned");
    }

    // Infrastructure Locations
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchRoadLocations() {
        String sql = """
            SELECT id AS roadId, road_name AS roadName, type, road_condition AS roadCondition,
                   length, width, ward, status,
                   lat, lng
            FROM road
            WHERE lat IS NOT NULL AND lng IS NOT NULL
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "roadId", "roadName", "type", "roadCondition", "length", "width", "ward", "status", "lat", "lng");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchDrainageLocations() {
        String sql = """
            SELECT id AS drainageId, name, ward, type, status, lat, lng
            FROM drainage
            WHERE lat IS NOT NULL AND lng IS NOT NULL
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "drainageId", "name", "ward", "type", "status", "lat", "lng");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchStreetLightLocations() {
        String sql = """
            SELECT id AS lightId, name, ward, status, location,
                   light_type AS lightType, problem_type AS problemType,
                   priority, count, lat, lng
            FROM street_light
            WHERE lat IS NOT NULL AND lng IS NOT NULL
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "lightId", "name", "ward", "status", "location",
                "lightType", "problemType", "priority", "count", "lat", "lng");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchConstructionSites() {
        String sql = """
            SELECT id AS siteId, applicant_name AS applicantName, ward, status,
                   location, building_type AS buildingType, floors, area,
                   DATE(created_at) AS appliedDate, lat, lng
            FROM construction
            WHERE lat IS NOT NULL AND lng IS NOT NULL
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "siteId", "applicantName", "ward", "status",
                "location", "buildingType", "floors", "area", "appliedDate", "lat", "lng");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchComplaintLocations() {
        String sql = """
            SELECT id AS complaintId, name, ward, area, category, description,
                   location, status, created_at AS createdAt, lat, lng
            FROM complaints
            WHERE lat IS NOT NULL AND lng IS NOT NULL
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "complaintId", "name", "ward", "area", "category", "description",
                "location", "status", "createdAt", "lat", "lng");
    }

    // Health & Waste
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchHealthCenterLocations() {
        String sql = """
            SELECT id AS centerId, name AS centerName, type, location, contact, status, lat, lng
            FROM health_center
            WHERE lat IS NOT NULL AND lng IS NOT NULL
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "centerId", "centerName", "type", "location", "contact", "status", "lat", "lng");
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchGarbageZones() {
        String sql = """
            SELECT id AS zoneId, ward, day AS scheduleDay, time AS scheduleTime,
                   area, lat, lng
            FROM garbage_schedule
            WHERE lat IS NOT NULL AND lng IS NOT NULL
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "zoneId", "ward", "scheduleDay", "scheduleTime", "area", "lat", "lng");
    }


    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchPickupRequestLocations() {
        String sql = """
            SELECT id AS requestId, name, address, ward, phone, type, status, created_at AS createdAt, lat, lng
            FROM waste_request
            WHERE lat IS NOT NULL AND lng IS NOT NULL
            """;
        return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                "requestId", "name", "address", "ward", "phone", "type", "status", "createdAt", "lat", "lng");
    }

    // Ward Boundaries
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchWardBoundaries() {
        try {
            String sql = """
                SELECT ward_no AS wardNo, population, area_sqkm AS areaSqKm,
                       boundary_geojson AS coordinates
                FROM ward_boundary ORDER BY ward_no
                """;
            return toMapList(entityManager.createNativeQuery(sql).getResultList(),
                    "wardNo", "population", "areaSqKm", "coordinates");
        } catch (Exception e) {
            return Collections.emptyList();
        }
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
