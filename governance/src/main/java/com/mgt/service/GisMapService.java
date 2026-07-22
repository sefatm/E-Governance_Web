package com.mgt.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mgt.dao.GisMapDAO;

@Service
public class GisMapService {

    @Autowired
    private GisMapDAO gisMapDAO;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Holding Map
    public Map<String, Object> getHoldingGeoJSON() {
        List<Map<String, Object>> rows = gisMapDAO.fetchHoldingLocations();
        return buildFeatureCollection(rows, "Point");
    }

    public Map<String, Object> getHoldingsByWard(int wardNo) {
        List<Map<String, Object>> rows = gisMapDAO.fetchHoldingsByWard(wardNo);
        return buildFeatureCollection(rows, "Point");
    }

    public Map<String, Object> getHoldingsByTaxStatus(String status) {
        List<Map<String, Object>> rows = gisMapDAO.fetchHoldingsByTaxStatus(status);
        return buildFeatureCollection(rows, "Point");
    }

    // Infrastructure Map 
    public Map<String, Object> getRoadGeoJSON() {
        List<Map<String, Object>> rows = gisMapDAO.fetchRoadLocations();
        return buildFeatureCollection(rows, "LineString");
    }

    public Map<String, Object> getDrainageGeoJSON() {
        List<Map<String, Object>> rows = gisMapDAO.fetchDrainageLocations();
        return buildFeatureCollection(rows, "Point");
    }

    public Map<String, Object> getStreetLightGeoJSON() {
        List<Map<String, Object>> rows = gisMapDAO.fetchStreetLightLocations();
        return buildFeatureCollection(rows, "Point");
    }

    public Map<String, Object> getConstructionGeoJSON() {
        List<Map<String, Object>> rows = gisMapDAO.fetchConstructionSites();
        return buildFeatureCollection(rows, "Point");
    }

    public Map<String, Object> getAllInfrastructureGeoJSON() {
        List<Map<String, Object>> all = new ArrayList<>();

        tagLayer(gisMapDAO.fetchRoadLocations(),       "road",          all);
        tagLayer(gisMapDAO.fetchDrainageLocations(),   "drainage",      all);
        tagLayer(gisMapDAO.fetchStreetLightLocations(),"street-light",  all);
        tagLayer(gisMapDAO.fetchConstructionSites(),   "construction",  all);
        
        return buildFeatureCollection(all, "Point");
    }

    public Map<String, Object> getComplaintGeoJSON() {
        return buildFeatureCollection(gisMapDAO.fetchComplaintLocations(), "Point");
    }

    // Health & Waste Map 

    public Map<String, Object> getHealthCenterGeoJSON() {
        return buildFeatureCollection(gisMapDAO.fetchHealthCenterLocations(), "Point");
    }

    public Map<String, Object> getGarbageZoneGeoJSON() {
        return buildFeatureCollection(gisMapDAO.fetchGarbageZones(), "Polygon");
    }

    public Map<String, Object> getWastePickupGeoJSON() {
        return buildFeatureCollection(gisMapDAO.fetchPickupRequestLocations(), "Point");
    }

    // Ward Boundaries 

    public Map<String, Object> getWardBoundaryGeoJSON() {
        return buildFeatureCollection(gisMapDAO.fetchWardBoundaries(), "Polygon");
    }

    // Helpers 
    private Map<String, Object> buildFeatureCollection(
            List<Map<String, Object>> rows, String defaultType) {

        List<Map<String, Object>> features = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Map<String, Object> feature = new LinkedHashMap<>();
            feature.put("type", "Feature");

            Map<String, Object> geometry = new LinkedHashMap<>();
            if (row.containsKey("coordinates")) {
                String geomType = (String) row.getOrDefault("geometryType", defaultType);
                geometry.put("type", geomType);
                Object coordinates = row.get("coordinates");
                if (coordinates instanceof String) {
                    try {
                        coordinates = objectMapper.readValue((String) coordinates, Object.class);
                    } catch (Exception ignored) {
                        coordinates = Collections.emptyList();
                    }
                }
                geometry.put("coordinates", coordinates);
            } else {
                double lat = toDouble(row.get("lat"));
                double lng = toDouble(row.get("lng"));
                geometry.put("type", "Point");
                geometry.put("coordinates", List.of(lng, lat));
            }
            feature.put("geometry", geometry);

            Map<String, Object> props = new LinkedHashMap<>(row);
            props.remove("lat");
            props.remove("lng");
            props.remove("coordinates");
            props.remove("geometryType");
            feature.put("properties", props);

            features.add(feature);
        }

        Map<String, Object> fc = new LinkedHashMap<>();
        fc.put("type", "FeatureCollection");
        fc.put("features", features);
        return fc;
    }

    private void tagLayer(List<Map<String, Object>> rows, String layerName, List<Map<String, Object>> target) {
        for (Map<String, Object> row : rows) {
            row.put("layer", layerName);
            target.add(row);
        }
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
