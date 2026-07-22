package com.mgt.controller;
import java.util.List; import org.springframework.web.bind.annotation.*; import org.springframework.beans.factory.annotation.Autowired; import org.springframework.http.ResponseEntity;
import com.mgt.model.WasteCollectionLog; import com.mgt.service.WasteCollectionLogService;
@RestController @RequestMapping("/api/waste-collection-log")
public class WasteCollectionLogController {
 @Autowired WasteCollectionLogService service;
 @GetMapping("/getall") public List<WasteCollectionLog> all(){return service.getAll();}
 @PostMapping("/create") public ResponseEntity<WasteCollectionLog> create(@RequestBody WasteCollectionLog l){return ResponseEntity.ok(service.create(l));}
}
