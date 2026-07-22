package com.mgt.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ward_boundary")
public class WardBoundary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // ✅ FK: ward_boundary.ward_no → ward.ward_number
    // Ward.ward_number তে UNIQUE KEY আছে, তাই FK রেফারেন্স করা যাবে
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_no", referencedColumnName = "ward_number", nullable = false,
                foreignKey = @ForeignKey(name = "fk_boundary_ward"))
    private Ward ward;

    @Column(name = "population")
    private Integer population;

    @Column(name = "area_sqkm")
    private Double areaSqkm;

    @Column(name = "boundary_geojson", columnDefinition = "TEXT")
    private String boundaryGeojson;

    // ── Getters & Setters ──────────────────────────────────────

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public Ward getWard()                       { return ward; }
    public void setWard(Ward ward)              { this.ward = ward; }

    public Integer getWardNo() {
        return ward != null ? ward.getWardNumber() : null;
    }

    public Integer getPopulation()              { return population; }
    public void setPopulation(Integer v)        { this.population = v; }

    public Double getAreaSqkm()                 { return areaSqkm; }
    public void setAreaSqkm(Double v)           { this.areaSqkm = v; }

    public String getBoundaryGeojson()          { return boundaryGeojson; }
    public void setBoundaryGeojson(String v)    { this.boundaryGeojson = v; }
}
