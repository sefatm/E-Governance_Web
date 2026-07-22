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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.ETenderBid;
import com.mgt.service.ETenderBidService;

@RestController
@RequestMapping("/api/etender/bid")
public class ETenderBidController {

    @Autowired
    private ETenderBidService service;

    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> submit(@RequestBody ETenderBid bid) {
        try {
            return ResponseEntity.ok(service.submitBid(bid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/submit-with-doc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> submitWithDoc(
            @RequestParam("tenderId")        int    tenderId,
            @RequestParam("bidderName")      String bidderName,
            @RequestParam("companyName")     String companyName,
            @RequestParam("nid")             String nid,
            @RequestParam("mobile")          String mobile,
            @RequestParam("email")           String email,
            @RequestParam("bidAmount")       Double bidAmount,
            @RequestParam("completionDays")  int    completionDays,
            @RequestParam("experienceYears") int    experienceYears,
            @RequestParam(value = "previousWorks", required = false) String previousWorks,
            @RequestParam(value = "emdReceiptNo",  required = false) String emdReceiptNo,
            @RequestParam(value = "document",      required = false) MultipartFile document
    ) {
        String docUrl = null;
        if (document != null && !document.isEmpty()) {
            try {
                Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath();
                Files.createDirectories(uploadPath);
                String filename = "bid_doc_" + UUID.randomUUID() + "_" + document.getOriginalFilename();
                Files.copy(document.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                docUrl = "uploads/" + filename;
            } catch (IOException e) {
                return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Document upload failed: " + e.getMessage()));
            }
        }

        ETenderBid bid = new ETenderBid();
        bid.setTenderId(tenderId);
        bid.setBidderName(bidderName);
        bid.setCompanyName(companyName);
        bid.setNid(nid);
        bid.setMobile(mobile);
        bid.setEmail(email);
        bid.setBidAmount(bidAmount);
        bid.setCompletionDays(completionDays);
        bid.setExperienceYears(experienceYears);
        bid.setPreviousWorks(previousWorks);
        bid.setEmdReceiptNo(emdReceiptNo);
        bid.setDocumentUrl(docUrl);

        try {
            return ResponseEntity.ok(service.submitBid(bid));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Admin only — document verify
    @PutMapping("/verify-doc/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<Object> verifyDocument(@PathVariable int id, @RequestBody Map<String, Object> body) {
        try {
            Boolean verified = (Boolean) body.get("verified");
            String  remark   = (String)  body.get("remark");
            if (verified == null)
                return ResponseEntity.badRequest().body(Map.of("message", "'verified' field দিতে হবে।"));
            service.verifyDocument(id, verified, remark);
            String msg = Boolean.TRUE.equals(verified)
                ? "Document Verified।"
                : "Document Rejected। Bidder কে email পাঠানো হয়েছে।";
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Public — lowest bid দেখা
    @GetMapping("/lowest/{tenderId}")
    public ResponseEntity<Object> getLowest(@PathVariable int tenderId) {
        ETenderBid lowest = service.getLowestBid(tenderId);
        if (lowest == null)
            return ResponseEntity.ok(Map.of("message", "এই Tender এ কোনো bid নেই।"));
        return ResponseEntity.ok(lowest);
    }

    // Admin only — সব bids দেখা
    @GetMapping("/getall")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<List<ETenderBid>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/tender/{tenderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ETenderBid>> getByTenderId(@PathVariable int tenderId) {
        return ResponseEntity.ok(service.getByTenderId(tenderId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        ETenderBid bid = service.getById(id);
        if (bid == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(bid);
    }

    // Admin only — status update
    @PutMapping("/status/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<Object> updateStatus(
            @PathVariable int id,
            @RequestBody Map<String, String> body) {
        service.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Bid status updated"));
    }

    @GetMapping("/count/{tenderId}")
    public ResponseEntity<Object> getBidCount(@PathVariable int tenderId) {
        return ResponseEntity.ok(Map.of("count", service.getBidCount(tenderId)));
    }
}
