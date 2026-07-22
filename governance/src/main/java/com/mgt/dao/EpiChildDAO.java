package com.mgt.dao;

import com.mgt.model.EpiChild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EpiChildDAO extends JpaRepository<EpiChild, Integer> {

    List<EpiChild>     findAllByOrderByCreatedAtDesc();
    Optional<EpiChild> findByCardNo(String cardNo);
    List<EpiChild>     findByGuardianNid(String nid);
    List<EpiChild>     findByWard(String ward);

    @Query("SELECT c FROM EpiChild c WHERE " +
           "LOWER(c.childName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
           "c.guardianNid LIKE CONCAT('%',:q,'%') OR " +
           "c.cardNo LIKE CONCAT('%',:q,'%') OR " +
           "c.guardianPhone LIKE CONCAT('%',:q,'%')")
    List<EpiChild> search(@Param("q") String query);

    long countByWard(String ward);
    List<EpiChild> findByStatus(String status);
    List<EpiChild> findByStatusOrderByCreatedAtDesc(String status);
}
