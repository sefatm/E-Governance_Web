package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.VendorBlacklistDAO;
import com.mgt.model.VendorBlacklist;

@Service
public class VendorBlacklistService {

    @Autowired
    private VendorBlacklistDAO dao;

    /** Vendor কে blacklist এ add করো */
    public VendorBlacklist blacklist(VendorBlacklist vendor) {
        return dao.save(vendor);
    }

    public List<VendorBlacklist> getAll() {
        return dao.getAll();
    }

    public List<VendorBlacklist> getActive() {
        return dao.getActive();
    }

    public VendorBlacklist getById(int id) {
        return dao.getById(id);
    }

    /**
     * Bid submit এর আগে এটা call করো
     * @return true = blocked, bid জমা নেওয়া যাবে না
     */
    public boolean isBlacklisted(String nid, String email, String mobile) {
        return dao.isBlacklisted(nid, email, mobile);
    }

    /** Vendor কে unblock করো (active = false) */
    public void unblock(int id) {
        dao.deactivate(id);
    }

    public void delete(int id) {
        dao.delete(id);
    }
}
