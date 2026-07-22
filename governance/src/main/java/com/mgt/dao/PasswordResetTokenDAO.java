package com.mgt.dao;

import com.mgt.model.PasswordResetToken;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class PasswordResetTokenDAO {

    @PersistenceContext
    private EntityManager entityManager;

    // Token save করো
    public void save(PasswordResetToken token) {
        entityManager.persist(token);
    }

    // Token string দিয়ে খোঁজো
    public PasswordResetToken findByToken(String token) {
        List<PasswordResetToken> list = entityManager
            .createQuery(
                "from PasswordResetToken t where t.token = :token",
                PasswordResetToken.class)
            .setParameter("token", token)
            .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    // একটি email এর সব পুরনো token delete করো (নতুন request এলে)
    public void deleteByEmail(String email) {
        entityManager
            .createQuery("delete from PasswordResetToken t where t.email = :email")
            .setParameter("email", email)
            .executeUpdate();
    }

    // Token use হয়ে গেলে mark করো (delete না করে audit trail রাখো)
    public void markUsed(String token) {
        PasswordResetToken t = findByToken(token);
        if (t != null) {
            t.setUsed(true);
            entityManager.merge(t);
        }
    }
}
