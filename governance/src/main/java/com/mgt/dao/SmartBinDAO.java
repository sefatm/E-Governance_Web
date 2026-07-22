package com.mgt.dao;
import java.util.List;
import org.springframework.stereotype.Repository;
import com.mgt.model.SmartBin;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
@Repository @Transactional
public class SmartBinDAO {
 @PersistenceContext EntityManager em;
 public SmartBin save(SmartBin b){ if(b.getId()==null){em.persist(b); return b;} return em.merge(b); }
 public List<SmartBin> getAll(){return em.createQuery("from SmartBin order by id desc", SmartBin.class).getResultList();}
 public SmartBin get(Integer id){return em.find(SmartBin.class,id);}
 public void delete(Integer id){SmartBin b=get(id); if(b!=null) em.remove(b);}
}
