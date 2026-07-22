package com.mgt.dao;

import com.mgt.model.EpiVaccination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface EpiVaccinationDAO extends JpaRepository<EpiVaccination, Integer> {

    @Query("SELECT v FROM EpiVaccination v JOIN FETCH v.child c WHERE c.id = :childId ORDER BY v.scheduledDate ASC")
    List<EpiVaccination> findByChild_IdOrderByScheduledDateAsc(@Param("childId") Integer childId);

    List<EpiVaccination> findByStatus(String status);

    @Query("SELECT v FROM EpiVaccination v JOIN FETCH v.child WHERE v.scheduledDate BETWEEN :from AND :to AND v.status IN ('Scheduled', 'Due') ORDER BY v.scheduledDate ASC")
    List<EpiVaccination> findUpcoming(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT v FROM EpiVaccination v JOIN FETCH v.child WHERE v.scheduledDate < :today AND v.status IN ('Scheduled', 'Due', 'Missed') ORDER BY v.scheduledDate ASC")
    List<EpiVaccination> findMissed(@Param("today") LocalDate today);

    long countByStatus(String status);

    @Query("SELECT COUNT(v) FROM EpiVaccination v WHERE v.status = 'Scheduled' AND v.scheduledDate < :today")
    long countMissed(@Param("today") LocalDate today);

    @Query("SELECT v FROM EpiVaccination v WHERE v.child.id = :childId AND v.vaccineName = :name AND v.doseNo = :dose")
    List<EpiVaccination> findByChildAndVaccine(@Param("childId") Integer childId, @Param("name") String name, @Param("dose") String dose);
}
