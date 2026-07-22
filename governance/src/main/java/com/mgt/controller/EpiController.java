package com.mgt.controller;

import com.mgt.dao.EpiVaccinationDAO;
import com.mgt.model.EpiChild;
import com.mgt.model.EpiVaccination;
import com.mgt.service.EpiCardPdfService;
import com.mgt.service.EpiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/epi")
public class EpiController {

    @Autowired EpiService         service;
    @Autowired EpiCardPdfService  pdfService;
    @Autowired EpiVaccinationDAO  vaccDAO;

    @Value("${citizen.upload.dir:src/main/resources/uploads/}")
    private String uploadDir;

    // ── Register child (status = Pending) ───────────────────
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody EpiChild child) {
        try {
            return ResponseEntity.ok(service.register(child));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> registerMultipart(
            @RequestParam String childName,
            @RequestParam String dateOfBirth,
            @RequestParam String gender,
            @RequestParam String fatherName,
            @RequestParam String motherName,
            @RequestParam(required = false) String guardianNid,
            @RequestParam(required = false) String fatherNid,
            @RequestParam(required = false) String motherNid,
            @RequestParam String guardianPhone,
            @RequestParam(required = false) String guardianEmail,
            @RequestParam String ward,
            @RequestParam(required = false) String unionName,
            @RequestParam(required = false) String upazila,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String presentAddress,
            @RequestParam(required = false) String permanentAddress,
            @RequestParam(required = false) String birthPlace,
            @RequestParam(required = false) MultipartFile childPhoto,
            @RequestParam(required = false) MultipartFile fatherNidFile,
            @RequestParam(required = false) MultipartFile motherNidFile) {
        try {
            EpiChild child = new EpiChild();
            child.setChildName(childName);
            child.setDateOfBirth(java.time.LocalDate.parse(dateOfBirth));
            child.setGender(gender);
            child.setFatherName(fatherName);
            child.setMotherName(motherName);
            child.setGuardianNid(guardianNid);
            child.setFatherNid(fatherNid);
            child.setMotherNid(motherNid);
            child.setGuardianPhone(guardianPhone);
            child.setGuardianEmail(guardianEmail);
            child.setWard(ward);
            child.setUnionName(unionName);
            child.setUpazila(upazila);
            child.setDistrict(district);
            child.setAddress(address);
            child.setPresentAddress(presentAddress);
            child.setPermanentAddress(permanentAddress);
            child.setBirthPlace(birthPlace);
            child.setChildPhotoUrl(saveFile(childPhoto, "epi_child_photo_"));
            child.setFatherNidFileUrl(saveFile(fatherNidFile, "epi_father_nid_"));
            child.setMotherNidFileUrl(saveFile(motherNidFile, "epi_mother_nid_"));
            return ResponseEntity.ok(service.register(child));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Approve child (admin) → status = Approved + email ───
    @PutMapping("/approve/{id}")
    public ResponseEntity<Object> approve(@PathVariable Integer id, @RequestBody(required = false) Map<String, String> body) {
        try {
            return ResponseEntity.ok(service.approveChild(id, body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── All children ─────────────────────────────────────────
    @GetMapping("/children")
    public List<EpiChild> getAllChildren() {
        return service.getAllChildren();
    }

    // ── Pending children ─────────────────────────────────────
    @GetMapping("/children/pending")
    public List<EpiChild> getPending() {
        return service.getPendingChildren();
    }

    // ── Search ───────────────────────────────────────────────
    @GetMapping("/children/search")
    public List<EpiChild> search(@RequestParam String q) {
        return service.search(q);
    }

    // ── Get child by ID ──────────────────────────────────────
    @GetMapping("/children/{id}")
    public ResponseEntity<Object> getChild(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(service.getChildById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Check by card no (public) ────────────────────────────
    @GetMapping("/check/{cardNo}")
    public ResponseEntity<Object> checkByCard(@PathVariable String cardNo) {
        return service.getByCardNo(cardNo)
                .map(c -> ResponseEntity.ok((Object) c))
                .orElse(ResponseEntity.notFound().build());
    }


    // ── QR scan lookup: accepts EPI:<cardNo> or plain card number ──
    @GetMapping("/scan/{payload}")
    public ResponseEntity<Object> scanCard(@PathVariable String payload) {
        try {
            String cardNo = payload != null && payload.startsWith("EPI:")
                    ? payload.substring(4) : payload;
            EpiChild child = service.getByCardNo(cardNo)
                    .orElseThrow(() -> new RuntimeException("EPI card not found"));
            if (!"Approved".equalsIgnoreCase(child.getStatus())) {
                throw new RuntimeException("EPI registration is not approved yet");
            }
            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("child", child);
            result.put("schedule", service.getVaccinationSchedule(child.getId()));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Vaccination schedule ─────────────────────────────────
    @GetMapping("/schedule/{childId}")
    public List<Map<String, Object>> getSchedule(@PathVariable Integer childId) {
        return service.getVaccinationSchedule(childId);
    }

    // ── Generate EPI card PDF ────────────────────────────────
    @GetMapping("/generate-pdf/{childId}")
    public ResponseEntity<byte[]> generatePdf(@PathVariable Integer childId) {
        try {
            EpiChild child = service.getChildById(childId);
            List<EpiVaccination> vaccinations =
                vaccDAO.findByChild_IdOrderByScheduledDateAsc(childId);

            byte[] pdf = pdfService.generate(child, vaccinations);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"EPI-Card-" + child.getCardNo() + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ── Mark vaccine as given ────────────────────────────────
    @PutMapping("/vaccinate/{vaccId}")
    public ResponseEntity<Object> markGiven(@PathVariable Integer vaccId,
                                             @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(service.markGiven(vaccId, body));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Mark as missed ───────────────────────────────────────
    @PutMapping("/missed/{vaccId}")
    public ResponseEntity<Object> markMissed(@PathVariable Integer vaccId) {
        try {
            return ResponseEntity.ok(service.markMissed(vaccId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Dashboard stats ──────────────────────────────────────
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return service.getDashboardStats();
    }

    // ── Upcoming (next 7 days) ───────────────────────────────
    @GetMapping("/upcoming")
    public List<Map<String, Object>> getUpcoming() {
        return service.getUpcomingVaccinations();
    }

    // ── Missed / overdue ─────────────────────────────────────
    @GetMapping("/missed")
    public List<Map<String, Object>> getMissed() {
        return service.getMissedVaccinations();
    }

    // ── Delete child ─────────────────────────────────────────
    @DeleteMapping("/children/{id}")
    public ResponseEntity<Object> delete(@PathVariable Integer id) {
        try {
            service.deleteChild(id);
            return ResponseEntity.ok(Map.of("message", "Deleted"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private String saveFile(MultipartFile file, String prefix) throws Exception {
        if (file == null || file.isEmpty()) return null;
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0) ext = original.substring(dot);
        String filename = prefix + UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), uploadPath.resolve(filename), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return "uploads/" + filename;
    }
}
