package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.OwnershipTransfer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "ownershipTransferDAO")
@Transactional
public class OwnershipTransferDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(OwnershipTransfer owner) {
        entityManager.persist(owner);
    }

    public OwnershipTransfer update(OwnershipTransfer owner) {
        return entityManager.merge(owner);
    }

    public List<OwnershipTransfer> getall() {
        return entityManager
                .createQuery("from OwnershipTransfer order by createdAt desc", OwnershipTransfer.class)
                .getResultList();
    }

    public OwnershipTransfer getById(int id) {
        return entityManager.find(OwnershipTransfer.class, id);
    }

    public void updateStatus(int id, String status, String rejectReason) {
        OwnershipTransfer owner = entityManager.find(OwnershipTransfer.class, id);
        if (owner != null) {
            owner.setStatus(status);
            if (rejectReason != null && !rejectReason.isBlank()) {
                owner.setRejectReason(rejectReason);
            }
            entityManager.merge(owner);
        }
    }
}
