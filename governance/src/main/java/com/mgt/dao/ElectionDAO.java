package com.mgt.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mgt.model.Election;
@Repository
public interface ElectionDAO extends JpaRepository<Election, Integer>{

	List<Election> findByStatus(String status);
    List<Election> findByStatusAndType(String status, String type);
    List<Election> findAllByOrderByCreatedAtDesc();
}
