package com.mgt.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.Nominee;
import com.mgt.model.VotingCenter;
import com.mgt.service.NomineeService;


@RestController
@RequestMapping("/api/nominee")
public class NomineeController {

    @Autowired
    private NomineeService nomineeService;

    @Value("${citizen.upload.dir:src/main/resources/uploads/}")
    private String uploadDir;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> create(
            @RequestParam("name")                        String name,
            @RequestParam("fathersName")                 String fathersName,
            @RequestParam("mothersName")                 String mothersName,
            @RequestParam("nid")                         String nid,
            @RequestParam("mobileNumber")                String mobileNumber,
            @RequestParam("dob")                         String dob,
            @RequestParam("electionType")                String electionType,
            @RequestParam("area")                        String area,
            @RequestParam("party")                       String party,
            @RequestParam(value = "symbol",   required = false) String symbol,
            @RequestParam(value = "zoneId",   required = false) Integer zoneId,
            @RequestParam(value = "centerId", required = false) Integer centerId,
            @RequestParam(value = "declaration",         required = false) String declaration,
            @RequestParam(value = "hasCriminalRecord",   required = false, defaultValue = "false") String hasCriminalRecord,
            @RequestParam(value = "symbolFile", required = false) MultipartFile symbolFile
    ) {
        String symbolFileUrl = null;
        if (symbolFile != null && !symbolFile.isEmpty()) {
            symbolFileUrl = saveFile(symbolFile, "sym");
            if (symbolFileUrl == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("message", "Symbol image upload failed"));
            }
        }

        Nominee nominee = new Nominee();
        nominee.setName(name);
        nominee.setFathersName(fathersName);
        nominee.setMothersName(mothersName);
        nominee.setNid(nid);
        nominee.setMobileNumber(mobileNumber);
        nominee.setDob(dob);
        nominee.setElectionType(electionType);
        nominee.setArea(area);
        nominee.setParty(party);
        nominee.setSymbol(symbol);
        nominee.setSymbolFileUrl(symbolFileUrl);          
        // FIX: setId(zoneId) ছিল — zoneId কে nominee.id তে set করা হচ্ছিল!
        // এটাই Optimistic Lock conflict এর কারণ ছিল।
        if (zoneId != null) {
            com.mgt.model.VotingZone zone = new com.mgt.model.VotingZone();
            zone.setId(zoneId);
            nominee.setZone(zone);
        }
        if (centerId != null) {
            com.mgt.model.VotingCenter center = new com.mgt.model.VotingCenter();
            center.setId(centerId);
            nominee.setCenter(center);
        }
        nominee.setDeclaration(declaration);
        nominee.setHasCriminalRecord("true".equalsIgnoreCase(hasCriminalRecord));

        try {
            return ResponseEntity.ok(nomineeService.save(nominee));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Nominee>> getAll() {
        return ResponseEntity.ok(nomineeService.getAll());
    }

    @GetMapping("/approved/{electionId}")
    public ResponseEntity<List<Nominee>> getApproved(@PathVariable Integer electionId) {
        return ResponseEntity.ok(nomineeService.getApprovedForElection(electionId));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<Object> approve(@PathVariable Integer id) {
        return ResponseEntity.ok(nomineeService.approve(id));
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<Object> reject(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(nomineeService.reject(id, body.get("reason")));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        nomineeService.delete(id);
        return ResponseEntity.ok("Deleted");
    }

    private String saveFile(MultipartFile file, String prefix) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);
            String ext      = getExtension(file.getOriginalFilename());
            String filename = prefix + "_" + UUID.randomUUID() + "." + ext;
            Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            return "uploads/" + filename;
        } catch (IOException e) {
            return null;
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
