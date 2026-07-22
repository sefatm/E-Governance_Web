package com.mgt.dao;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.mgt.model.WasteCollectionLog;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
@Repository @Transactional
public class WasteCollectionLogDAO {
 @PersistenceContext EntityManager em;
 public WasteCollectionLog save(WasteCollectionLog l){em.persist(l); return l;}
 public List<WasteCollectionLog> getAll(){return em.createQuery("from WasteCollectionLog order by collectionDate desc", WasteCollectionLog.class).getResultList();}
 public boolean existsByPickupRequestId(Integer id){return em.createQuery("select count(l) from WasteCollectionLog l where l.pickupRequestId=:id",Long.class).setParameter("id",id).getSingleResult()>0;}
}
