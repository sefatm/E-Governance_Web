package com.mgt.service;
import java.time.LocalDateTime; import java.util.List;
import org.springframework.stereotype.Service; import org.springframework.beans.factory.annotation.Autowired;
import com.mgt.dao.WasteCollectionLogDAO; import com.mgt.model.WasteCollectionLog;
@Service public class WasteCollectionLogService {
 @Autowired WasteCollectionLogDAO dao;
 public List<WasteCollectionLog> getAll(){return dao.getAll();}
 public boolean existsForPickup(Integer id){return dao.existsByPickupRequestId(id);}
 public WasteCollectionLog create(WasteCollectionLog l){ if(l.getCollectionDate()==null)l.setCollectionDate(LocalDateTime.now()); if("Completed".equalsIgnoreCase(l.getStatus())&&l.getCompletedAt()==null)l.setCompletedAt(LocalDateTime.now()); return dao.save(l);} 
}
