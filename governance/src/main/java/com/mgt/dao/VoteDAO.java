package com.mgt.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mgt.model.Vote;

@Repository
public interface VoteDAO extends JpaRepository<Vote, Integer> {

    boolean existsByVoter_IdAndElection_Id(Integer voterId, Integer electionId);

    long countByElection_Id(Integer electionId);

    // FIX: voter delete করার আগে vote count check-এর জন্য
    long countByVoter_Id(Integer voterId);

    // Vote count by candidate for result page
    @Query("""
        SELECT v.candidate.id, COUNT(v)
        FROM Vote v
        WHERE v.election.id = :electionId
        GROUP BY v.candidate.id
        ORDER BY COUNT(v) DESC
    """)
    List<Object[]> getVoteCountByElection(@Param("electionId") Integer electionId);
}
