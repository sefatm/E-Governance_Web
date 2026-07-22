package com.mgt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "voting_center")
@NoArgsConstructor
@AllArgsConstructor
public class VotingCenter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id", foreignKey = @ForeignKey(name = "fk_center_zone"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private VotingZone zone;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VotingZone getZone() {
        return zone;
    }

    public void setZone(VotingZone zone) {
        this.zone = zone;
    }

    public Integer getZoneId() {
        return zone != null ? zone.getId() : null;
    }
}