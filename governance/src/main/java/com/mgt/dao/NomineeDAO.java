package com.mgt.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.mgt.model.Nominee;

@Repository
public interface NomineeDAO extends JpaRepository<Nominee, Integer> {

    List<Nominee> findByStatus(String status);

    List<Nominee> findAllByOrderByCreatedAtDesc();

    List<Nominee> findByStatusAndElectionType(String status, String electionType);

    @Query("SELECT n FROM Nominee n WHERE n.status = 'APPROVED' ORDER BY n.createdAt DESC")
    List<Nominee> findAllApproved();
}
