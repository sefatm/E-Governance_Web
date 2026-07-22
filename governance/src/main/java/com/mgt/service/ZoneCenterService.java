package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.VotingCenterDAO;
import com.mgt.dao.VotingZoneDAO;
import com.mgt.model.VotingCenter;
import com.mgt.model.VotingZone;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ZoneCenterService {

	@Autowired
    private VotingZoneDAO zoneDAO;

    @Autowired
    private VotingCenterDAO centerDAO;

    // zones
    public List<VotingZone> getAllZones() {
        return zoneDAO.findAll();
    }

    // centers
    public List<VotingCenter> getAllCenters() {
        return centerDAO.findAll();
    }

    // Get centers by zone id
    public List<VotingCenter> getCentersByZone(Integer zoneId) {
        return centerDAO.findByZone_Id(zoneId);
    }

    // Save zone
    public VotingZone saveZone(VotingZone votingZone) {
        return zoneDAO.save(votingZone);
    }

    // Save center
    public VotingCenter saveCenter(VotingCenter votingCenter) {
        return centerDAO.save(votingCenter);
    }
}
