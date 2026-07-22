package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.PassportApply;
import com.mgt.service.PassportService;

@RestController
@RequestMapping(value = "/api/passport")
public class PassportController {

    @Autowired
    PassportService passportService;

    @PostMapping("/create")
    public void create(@RequestBody PassportApply passport) {
        passportService.create(passport);
    }

    @GetMapping("/getall")
    public List<PassportApply> getall() {
        return passportService.getall();
    }

    @GetMapping("/mobile/{mobile}")
    public List<PassportApply> getByMobile(@PathVariable String mobile) {
        return passportService.findByContact(mobile);
    }

    @PutMapping("/status/{id}")
    public void updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        passportService.updateStatus(id, body.get("status"));
    }

    @PutMapping("/approve/{id}")
    public void approve(@PathVariable int id) {
        passportService.approve(id);
    }

    @PutMapping("/reject/{id}")
    public void reject(@PathVariable int id, @RequestBody Map<String, String> body) {
        passportService.reject(id, body.get("reason"));
    }

    @PutMapping("/update/{id}")
    public void update(@PathVariable int id, @RequestBody PassportApply passport) {
        passportService.update(id, passport);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        passportService.delete(id);
    }

    // POST /api/passport/upload/{id}
    @PostMapping(value = "/upload/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadFiles(
            @PathVariable int id,
            @RequestParam(value = "photo",     required = false) MultipartFile photo,
            @RequestParam(value = "nidFile",   required = false) MultipartFile nidFile,
            @RequestParam(value = "birthFile", required = false) MultipartFile birthFile) {
        try {
            Path up = Paths.get("src/main/resources/uploads").toAbsolutePath();
            Files.createDirectories(up);

            String photoUrl     = null;
            String nidFileUrl   = null;
            String birthFileUrl = null;

            if (photo != null && !photo.isEmpty()) {
                String fn = "pp_photo_" + UUID.randomUUID() + ext(photo);
                Files.copy(photo.getInputStream(), up.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                photoUrl = "uploads/" + fn;
            }
            if (nidFile != null && !nidFile.isEmpty()) {
                String fn = "pp_nid_" + UUID.randomUUID() + ext(nidFile);
                Files.copy(nidFile.getInputStream(), up.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                nidFileUrl = "uploads/" + fn;
            }
            if (birthFile != null && !birthFile.isEmpty()) {
                String fn = "pp_birth_" + UUID.randomUUID() + ext(birthFile);
                Files.copy(birthFile.getInputStream(), up.resolve(fn), StandardCopyOption.REPLACE_EXISTING);
                birthFileUrl = "uploads/" + fn;
            }

            passportService.updateFiles(id, photoUrl, nidFileUrl, birthFileUrl);

        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    private String ext(MultipartFile f) {
        String n = f.getOriginalFilename();
        return (n != null && n.contains(".")) ? n.substring(n.lastIndexOf('.')) : ".jpg";
    }
}
