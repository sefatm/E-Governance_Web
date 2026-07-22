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

import com.mgt.model.Complaint;
import com.mgt.service.ComplaintService;

@RestController
@RequestMapping(value = "/api/complaints")
public class ComplaintController {

    @Autowired
    ComplaintService complaintService;

    @Value("${citizen.upload.dir:src/main/resources/uploads/}")
    private String uploadDir;

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> create(
            @RequestParam("name")                         String name,
            @RequestParam("ward")                         String ward,
            @RequestParam("area")                         String area,
            @RequestParam("category")                     String category,
            @RequestParam("description")                  String description,
            @RequestParam("contact")                      String contact,
            @RequestParam("location")                     String location,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng,
            @RequestParam(value = "photo", required = false) MultipartFile photo
    ) {
        String imageUrl = null;

        if (photo != null && !photo.isEmpty()) {
            imageUrl = saveFile(photo);
            if (imageUrl == null) {
                return ResponseEntity.internalServerError()
                        .body(Map.of("message", "Image upload failed"));
            }
        }

        Complaint complaint = new Complaint();
        complaint.setName(name);
        complaint.setWard(ward);
        complaint.setArea(area);
        complaint.setCategory(category);
        complaint.setDescription(description);
        complaint.setContact(contact);
        complaint.setLocation(location);
        complaint.setLat(lat);
        complaint.setLng(lng);
        complaint.setImageUrl(imageUrl);   
        complaint.setStatus("Pending");

        complaintService.create(complaint);
        return ResponseEntity.ok(Map.of("message", "Complaint submitted successfully"));
    }

    @GetMapping("/getall")
    public List<Complaint> getall() {
        return complaintService.getall();
    }

    @GetMapping("/mobile/{mobile}")
    public ResponseEntity<Object> getByMobile(@PathVariable String mobile) {
        List<Complaint> result = complaintService.getByContact(mobile);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        complaintService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/remarks/{id}")
    public ResponseEntity<Object> updateRemarks(@PathVariable int id, @RequestBody Map<String, String> body) {
        complaintService.updateRemarks(id, body.get("remarks"));
        return ResponseEntity.ok(Map.of("message", "Remarks updated"));
    }


    @PutMapping("/location/{id}")
    public ResponseEntity<Object> updateLocation(@PathVariable int id, @RequestBody Map<String, Double> body) {
        Double lat = body.containsKey("lat") ? body.get("lat") : body.get("latitude");
        Double lng = body.containsKey("lng") ? body.get("lng") : body.get("longitude");
        if (lat == null || lng == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "lat and lng are required"));
        }
        complaintService.updateLocation(id, lat, lng);
        return ResponseEntity.ok(Map.of("message", "Complaint location updated"));
    }

    private String saveFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(uploadPath);
            String ext      = getExtension(file.getOriginalFilename());
            String filename = "complaint_" + UUID.randomUUID() + "." + ext;
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
