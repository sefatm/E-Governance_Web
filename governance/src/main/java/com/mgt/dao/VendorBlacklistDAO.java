package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.VendorBlacklist;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class VendorBlacklistDAO {

    @PersistenceContext
    private EntityManager em;

    /** নতুন vendor blacklist এ add করো */
    public VendorBlacklist save(VendorBlacklist v) {
        em.persist(v);
        return v;
    }

    /** সব active blacklisted vendors */
    public List<VendorBlacklist> getAll() {
        return em.createQuery("FROM VendorBlacklist ORDER BY blacklistedAt DESC", VendorBlacklist.class)
                 .getResultList();
    }

    /** শুধু active block গুলো */
    public List<VendorBlacklist> getActive() {
        return em.createQuery("FROM VendorBlacklist WHERE active = true ORDER BY blacklistedAt DESC", VendorBlacklist.class)
                 .getResultList();
    }

    public VendorBlacklist getById(int id) {
        return em.find(VendorBlacklist.class, id);
    }

    /**
     * Bid submit করার সময় check — NID, email, mobile যেকোনো একটা match হলে blocked
     * @return true মানে vendor blacklisted (bid block করো)
     */
    public boolean isBlacklisted(String nid, String email, String mobile) {
        String jpql = """
            SELECT COUNT(v) FROM VendorBlacklist v
            WHERE v.active = true
              AND (
                    (:nid IS NOT NULL AND v.nid = :nid)
                 OR (:email IS NOT NULL AND v.email = :email)
                 OR (:mobile IS NOT NULL AND v.mobile = :mobile)
              )
            """;
        long count = (long) em.createQuery(jpql)
            .setParameter("nid",    nid)
            .setParameter("email",  email)
            .setParameter("mobile", mobile)
            .getSingleResult();
        return count > 0;
    }

    /** Unblock — active = false করো */
    public void deactivate(int id) {
        VendorBlacklist v = em.find(VendorBlacklist.class, id);
        if (v != null) {
            v.setActive(false);
            em.merge(v);
        }
    }

    /** Delete permanently */
    public void delete(int id) {
        VendorBlacklist v = em.find(VendorBlacklist.class, id);
        if (v != null) em.remove(v);
    }
}
