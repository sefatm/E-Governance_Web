package com.mgt.dao;

import com.mgt.model.VotingCenter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VotingCenterDAO extends JpaRepository<VotingCenter, Integer> {

    // Zone-এর সব centers — ZoneCenterService এ ব্যবহার হচ্ছে
    List<VotingCenter> findByZone_Id(Integer zoneId);
}
