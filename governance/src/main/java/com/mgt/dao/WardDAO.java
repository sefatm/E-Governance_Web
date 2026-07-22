package com.mgt.dao;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import com.mgt.model.Ward;

@Repository
@Transactional
public class WardDAO {

    @PersistenceContext
    private EntityManager em;

    public Ward save(Ward ward) {
        if (ward.getId() == 0) { em.persist(ward); return ward; }
        return em.merge(ward);
    }

    public List<Ward> getAll() {
        return em.createQuery("from Ward order by number", Ward.class).getResultList();
    }

    public Ward getById(int id) { return em.find(Ward.class, id); }

    public Ward getByNumber(int number) {
        return em.createQuery("from Ward w where w.number = :number", Ward.class)
                 .setParameter("number", number)
                 .getResultStream()
                 .findFirst()
                 .orElse(null);
    }

    public void updateStatus(int id, String status) {
        Ward w = em.find(Ward.class, id);
        if (w != null) { w.setStatus(status); em.merge(w); }
    }

    public void delete(int id) {
        em.createQuery("delete from Ward where id = :id")
          .setParameter("id", id).executeUpdate();
    }

    public List<Ward> getAllByStatus(String status) {
        return em.createQuery("from Ward w where w.status = :s order by w.number", Ward.class)
                 .setParameter("s", status).getResultList();
    }

    /**
     * Returns all wards joined with their boundary GeoJSON.
     * Wards without a boundary row still appear (LEFT JOIN) with boundaryGeoJson = null.
     */
    @SuppressWarnings("unchecked")
    public List<Ward> getAllWithBoundaries() {
        String sql = """
            SELECT w.id, w.ward_number, w.name, w.area, w.population,
                   w.representative, w.contact, w.status, w.created_at,
                   (SELECT wb.boundary_geojson FROM ward_boundary wb
                    WHERE wb.ward_no = w.ward_number
                    ORDER BY wb.id DESC LIMIT 1) AS boundary_geojson
            FROM ward w
            ORDER BY w.ward_number
            """;

        List<Object[]> rows = em.createNativeQuery(sql).getResultList();
        List<Ward> result = new ArrayList<>();

        for (Object[] row : rows) {
            Ward w = new Ward();
            w.setId(((Number) row[0]).intValue());
            w.setNumber(((Number) row[1]).intValue());
            w.setName((String) row[2]);
            w.setArea(row[3] != null ? ((Number) row[3]).doubleValue() : null);
            w.setPopulation(row[4] != null ? ((Number) row[4]).intValue() : null);
            w.setRepresentative((String) row[5]);
            w.setContact((String) row[6]);
            w.setStatus((String) row[7]);
            // row[8] = created_at (ignored here)
            w.setBoundaryGeoJson((String) row[9]);
            result.add(w);
        }
        return result;
    }
}
