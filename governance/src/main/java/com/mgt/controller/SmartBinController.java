package com.mgt.controller;
import java.util.*; import org.springframework.web.bind.annotation.*; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.http.ResponseEntity;
import com.mgt.model.SmartBin; import com.mgt.service.SmartBinService;
@RestController @RequestMapping("/api/smart-bin")
public class SmartBinController {
 @Autowired SmartBinService service;
 @GetMapping("/getall") public List<SmartBin> all(){return service.getAll();}
 @PostMapping("/create") public ResponseEntity<SmartBin> create(@RequestBody SmartBin b){return ResponseEntity.ok(service.create(b));}
 @PutMapping("/fill-level/{id}") public ResponseEntity<SmartBin> fill(@PathVariable Integer id,@RequestBody Map<String,Integer> b){return ResponseEntity.ok(service.updateFill(id,b.get("fillLevel")));}
 @PutMapping("/collect/{id}") public ResponseEntity<SmartBin> collect(@PathVariable Integer id){return ResponseEntity.ok(service.collect(id));}
 @PutMapping("/maintenance/{id}") public ResponseEntity<SmartBin> maintenance(@PathVariable Integer id){return ResponseEntity.ok(service.maintenance(id));}
 @DeleteMapping("/delete/{id}") public ResponseEntity<?> delete(@PathVariable Integer id){service.delete(id);return ResponseEntity.ok(Map.of("message","Deleted"));}
}
