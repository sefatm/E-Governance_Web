package com.mgt.service;
import java.time.LocalDateTime; import java.util.List;
import org.springframework.stereotype.Service; import org.springframework.beans.factory.annotation.Autowired;
import com.mgt.dao.SmartBinDAO; import com.mgt.model.SmartBin;
@Service public class SmartBinService {
 @Autowired SmartBinDAO dao;
 public List<SmartBin> getAll(){return dao.getAll();}
 public SmartBin create(SmartBin b){normalize(b); return dao.save(b);} 
 public SmartBin updateFill(Integer id,Integer level){SmartBin b=req(id); int v=Math.max(0,Math.min(100,level==null?0:level)); b.setFillLevel(v); if(!"Maintenance".equalsIgnoreCase(b.getStatus())) b.setStatus(v>=80?"Full":"Normal"); b.setLastUpdated(LocalDateTime.now()); return dao.save(b);} 
 public SmartBin collect(Integer id){SmartBin b=req(id); b.setFillLevel(0); b.setStatus("Collected"); b.setLastCollected(LocalDateTime.now()); b.setLastUpdated(LocalDateTime.now()); return dao.save(b);} 
 public SmartBin maintenance(Integer id){SmartBin b=req(id); b.setStatus("Maintenance"); b.setLastUpdated(LocalDateTime.now()); return dao.save(b);} 
 public void delete(Integer id){dao.delete(id);} 
 private SmartBin req(Integer id){SmartBin b=dao.get(id); if(b==null) throw new IllegalArgumentException("Smart bin not found"); return b;}
 private void normalize(SmartBin b){ if(b.getFillLevel()==null)b.setFillLevel(0); if(b.getStatus()==null||b.getStatus().isBlank())b.setStatus(b.getFillLevel()>=80?"Full":"Normal"); b.setLastUpdated(LocalDateTime.now()); }
}
