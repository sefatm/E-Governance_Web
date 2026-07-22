package com.mgt.dao;

import com.mgt.model.VotingZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VotingZoneDAO extends JpaRepository<VotingZone, Integer> {
    // findById(id) inherited — Controller-এ ব্যবহার হচ্ছে
}
