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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.dao.VotingCenterDAO;
import com.mgt.dao.VotingZoneDAO;
import com.mgt.model.VoterRegistration;
import com.mgt.model.VotingCenter;
import com.mgt.model.VotingZone;
import com.mgt.service.VoterRegistrationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/voter")
// FIX: wildcard "*" এর বদলে নির্দিষ্ট origin — CORS attack প্রতিরোধ
// Production-এ এখানে actual domain দিন
@RequiredArgsConstructor
public class VoterRegistrationController {

    @Autowired private VoterRegistrationService voterService;
    @Autowired private VotingZoneDAO             zoneDAO;
    @Autowired private VotingCenterDAO           centerDAO;

    @Value("${citizen.upload.dir:src/main/resources/uploads/}")
    private String uploadDir;

    // ── Register (authenticated citizen) ─────────────────────────────────────
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> create(@RequestBody Map<String, Object> body) {
        try {
            VoterRegistration voter = buildVoter(body, null);
            return ResponseEntity.ok(voterService.save(voter));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> createWithPhoto(
            @RequestParam Map<String, String> body,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        try {
            VoterRegistration voter = buildVoter(body, photo);
            return ResponseEntity.ok(voterService.save(voter));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Get All (Admin only) ──────────────────────────────────────────────────
    @GetMapping("/getall")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<List<VoterRegistration>> getAll() {
        return ResponseEntity.ok(voterService.getAll());
    }

    // ── Approve (Admin only) ──────────────────────────────────────────────────
    @PutMapping("/approve/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<Object> approve(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(voterService.approve(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Reject (Admin only) ───────────────────────────────────────────────────
    @PutMapping("/reject/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer','ROLE_ElectionOfficer')")
    public ResponseEntity<Object> reject(@PathVariable Integer id,
                                         @RequestBody Map<String, String> body) {
        try {
            String reason = body.get("reason");
            if (reason == null || reason.isBlank())
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Rejection-এর কারণ দেওয়া আবশ্যক।"));
            return ResponseEntity.ok(voterService.reject(id, reason));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Delete (Admin only) ───────────────────────────────────────────────────
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_Super_Admin','ROLE_Admin_Municipal_Officer')")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            voterService.delete(id);
            return ResponseEntity.ok("Deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Verify (PUBLIC — voter login করে না) ─────────────────────────────────
    @PostMapping("/verify")
    public ResponseEntity<Object> verify(@RequestBody Map<String, String> body) {
        try {
            VoterRegistration voter = voterService.verify(body.get("nid"), body.get("dob"));
            return ResponseEntity.ok(Map.of(
                    "voterId", voter.getId(),
                    "name",    voter.getName(),
                    "email",   voter.getEmail() != null ? voter.getEmail() : "",
                    "message", "Verification Successful"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Has Voted ─────────────────────────────────────────────────────────────
    @GetMapping("/has-voted/{voterId}/{electionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> hasVoted(@PathVariable Integer voterId,
                                             @PathVariable Integer electionId) {
        return ResponseEntity.ok(voterService.hasVoted(voterId, electionId));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private String str(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v != null ? v.toString() : null;
    }
    private Integer intVal(Map<String, Object> body, String key) {
        Object v = body.get(key);
        if (v == null) return null;
        if (v instanceof Integer) return (Integer) v;
        try { return Integer.parseInt(v.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    private VoterRegistration buildVoter(Map<String, ?> body, MultipartFile photo) {
        VoterRegistration voter = new VoterRegistration();
        voter.setName(            strAny(body, "name"));
        voter.setDob(             strAny(body, "dob"));
        voter.setGender(          strAny(body, "gender"));
        voter.setFatherName(      strAny(body, "fatherName"));
        voter.setMotherName(      strAny(body, "motherName"));
        voter.setNid(             strAny(body, "nid"));
        voter.setMobile(          strAny(body, "mobile"));
        voter.setEmail(           strAny(body, "email"));
        voter.setDistrict(        strAny(body, "district"));
        voter.setUpazila(         strAny(body, "upazila"));
        voter.setArea(            strAny(body, "area"));
        voter.setAddress(         strAny(body, "address"));
        voter.setElectionType(    strAny(body, "electionType"));
        voter.setRegistrationDate(strAny(body, "registrationDate"));

        Integer zoneId = intAny(body, "zoneId");
        if (zoneId != null) {
            VotingZone zone = zoneDAO.findById(zoneId)
                    .orElseThrow(() -> new RuntimeException("Zone not found: " + zoneId));
            voter.setZone(zone);
        }

        Integer centerId = intAny(body, "centerId");
        if (centerId != null) {
            VotingCenter center = centerDAO.findById(centerId)
                    .orElseThrow(() -> new RuntimeException("Center not found: " + centerId));
            voter.setCenter(center);
        }

        if (photo != null && !photo.isEmpty()) {
            String photoUrl = saveFile(photo, "voter");
            if (photoUrl == null) {
                throw new RuntimeException("Voter photo upload failed");
            }
            voter.setPhotoUrl(photoUrl);
        }
        return voter;
    }

    private String strAny(Map<String, ?> body, String key) {
        Object v = body.get(key);
        return v != null ? v.toString() : null;
    }

    private Integer intAny(Map<String, ?> body, String key) {
        Object v = body.get(key);
        if (v == null || v.toString().isBlank()) return null;
        try { return Integer.parseInt(v.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    private String saveFile(MultipartFile file, String prefix) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);
            String ext = getExtension(file.getOriginalFilename());
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
