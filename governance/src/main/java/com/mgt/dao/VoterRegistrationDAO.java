package com.mgt.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mgt.model.VoterRegistration;

@Repository
public interface VoterRegistrationDAO extends JpaRepository<VoterRegistration, Integer> {

    List<VoterRegistration> findByStatus(String status);

    List<VoterRegistration> findAllByOrderByCreatedAtDesc();

    Optional<VoterRegistration> findByNidAndDob(String nid, String dob);

    boolean existsByNid(String nid);

    List<VoterRegistration> findByStatusAndElectionType(String status, String electionType);
}
