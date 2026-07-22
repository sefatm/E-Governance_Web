package com.mgt.controller;

import com.mgt.model.FarmerCard;
import com.mgt.service.FarmerCardPdfService;
import com.mgt.service.FarmerCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/farmer-card")
public class FarmerCardController {

    @Autowired FarmerCardService    svc;
    @Autowired FarmerCardPdfService pdfService;
    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    @PostMapping(value = "/apply", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> apply(
            @RequestParam("farmerName") String farmerName,
            @RequestParam("nid") String nid,
            @RequestParam(value="dateOfBirth",    required=false) String dateOfBirth,
            @RequestParam(value="fatherName",     required=false) String fatherName,
            @RequestParam("contact") String contact,
            @RequestParam("address") String address,
            @RequestParam(value="ward",           required=false) String ward,
            @RequestParam(value="unionName",      required=false) String unionName,
            @RequestParam(value="upazila",        required=false) String upazila,
            @RequestParam(value="district",       required=false) String district,
            @RequestParam(value="occupation",     required=false) String occupation,
            @RequestParam(value="incomeMonthly",  required=false) String incomeMonthly,
            @RequestParam(value="hasOtherCard",   required=false, defaultValue="false") String hasOtherCard,
            @RequestParam(value="landOwn",        required=false, defaultValue="0") String landOwn,
            @RequestParam(value="landLease",      required=false, defaultValue="0") String landLease,
            @RequestParam(value="landTotal",      required=false, defaultValue="0") String landTotal,
            @RequestParam(value="cropTypes",      required=false) String cropTypes,
            @RequestParam(value="farmingSeason",  required=false) String farmingSeason,
            @RequestParam(value="irrigationType", required=false) String irrigationType,
            @RequestParam(value="soilType",       required=false) String soilType,
            @RequestParam(value="previousCrop",   required=false) String previousCrop,
            @RequestParam(value="bankName",       required=false) String bankName,
            @RequestParam(value="bankAccount",    required=false) String bankAccount,
            @RequestParam(value="bankBranch",     required=false) String bankBranch,
            @RequestParam(value="photo",          required=false) MultipartFile photo,
            @RequestParam(value="nidFile",        required=false) MultipartFile nidFile,
            @RequestParam(value="landDoc",        required=false) MultipartFile landDoc
    ) {
        Path up = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        try { Files.createDirectories(up); } catch (Exception ignored) {}
        String photoUrl=null, nidUrl=null, landUrl=null;
        try {
            if (photo   != null && !photo.isEmpty())   photoUrl = saveFile(photo,   "frm_photo_", up);
            if (nidFile != null && !nidFile.isEmpty()) nidUrl   = saveFile(nidFile, "frm_nid_",   up);
            if (landDoc != null && !landDoc.isEmpty()) landUrl  = saveFile(landDoc, "frm_land_",  up);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("message","ফাইল আপলোড ব্যর্থ: "+e.getMessage()));
        }
        BigDecimal own=dec(landOwn), lease=dec(landLease);
        FarmerCard c = new FarmerCard();
        c.setFarmerName(farmerName); c.setNid(nid); c.setDateOfBirth(dateOfBirth);
        c.setFatherName(fatherName); c.setContact(contact); c.setAddress(address);
        c.setWard(ward); c.setUnionName(unionName); c.setUpazila(upazila); c.setDistrict(district);
        c.setOccupation(occupation); c.setIncomeMonthly(incomeMonthly);
        c.setHasOtherCard("true".equalsIgnoreCase(hasOtherCard));
        c.setLandOwn(own); c.setLandLease(lease); c.setLandTotal(own.add(lease));
        c.setCropTypes(cropTypes); c.setFarmingSeason(farmingSeason);
        c.setIrrigationType(irrigationType); c.setSoilType(soilType); c.setPreviousCrop(previousCrop);
        c.setBankName(bankName); c.setBankAccount(bankAccount); c.setBankBranch(bankBranch);
        c.setPhotoUrl(photoUrl); c.setNidFileUrl(nidUrl); c.setLandDocUrl(landUrl);
        c.setCreatedAt(LocalDateTime.now());
        if ("OK".equals(svc.create(c)))
            return ResponseEntity.ok(Map.of("message","আবেদন সফল।","cardNo",nvl(c.getCardNo())));
        return ResponseEntity.badRequest().body(Map.of("message","এই NID দিয়ে আগেই আবেদন করা হয়েছে।"));
    }

    @GetMapping("/getall")
    public List<FarmerCard> getAll() { return svc.getAll(); }

    @GetMapping("/status/{status}")
    public List<FarmerCard> getByStatus(@PathVariable String status) { return svc.getByStatus(status); }

    @GetMapping("/district/{district}")
    public List<FarmerCard> getByDistrict(@PathVariable String district) { return svc.getByDistrict(district); }

    @GetMapping("/ward/{ward}")
    public List<FarmerCard> getByWard(@PathVariable String ward) { return svc.getByWard(ward); }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        FarmerCard c = svc.getById(id);
        return c != null ? ResponseEntity.ok(c) : ResponseEntity.notFound().build();
    }

    @GetMapping("/check/{nid}")
    public ResponseEntity<Object> checkByNid(@PathVariable String nid) {
        FarmerCard c = svc.getByNid(nid);
        if (c == null) return ResponseEntity.notFound().build();
        Map<String,Object> res = new LinkedHashMap<>();
        res.put("id",              c.getId());
        res.put("cardNo",          nvl(c.getCardNo()));
        res.put("status",          nvl(c.getStatus()));
        res.put("farmerName",      nvl(c.getFarmerName()));
        res.put("fertilizerQuota", c.getFertilizerQuota() != null ? c.getFertilizerQuota() : 0);
        res.put("seedQuota",       c.getSeedQuota()       != null ? c.getSeedQuota()       : 0);
        res.put("landTotal",       c.getLandTotal());
        res.put("expireDate",      c.getExpireDate() != null ? c.getExpireDate().toString() : "");
        res.put("rejectionReason", nvl(c.getRejectionReason()));
        return ResponseEntity.ok(res);
    }

    @GetMapping("/expiring")
    public List<FarmerCard> expiringSoon(@RequestParam(defaultValue="30") int days) {
        return svc.getExpiringSoon(days);
    }

    @GetMapping("/duplicate-check/{nid}")
    public ResponseEntity<Object> duplicateCheck(@PathVariable String nid) {
        List<Object[]> hits = svc.checkDuplicateAcrossCards(nid);
        return ResponseEntity.ok(Map.of("duplicates", hits, "hasDuplicate", !hits.isEmpty()));
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String,String> body) {
        String status = body.get("status");
        if (status == null || status.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message","status আবশ্যক।"));
        svc.updateStatus(id, status, body.getOrDefault("approvedBy","Admin"), body.getOrDefault("rejectionReason",null), body.get("signatureBase64"));
        return ResponseEntity.ok(Map.of("message","স্ট্যাটাস আপডেট হয়েছে।"));
    }

    @PutMapping("/subsidy/{id}")
    public ResponseEntity<Object> updateSubsidy(@PathVariable int id, @RequestBody Map<String,Object> body) {
        FarmerCard c = svc.getById(id);
        if (c == null) return ResponseEntity.notFound().build();
        if (body.containsKey("fertilizerQuota"))
            c.setFertilizerQuota(BigDecimal.valueOf(((Number)body.get("fertilizerQuota")).doubleValue()));
        if (body.containsKey("seedQuota"))
            c.setSeedQuota(BigDecimal.valueOf(((Number)body.get("seedQuota")).doubleValue()));
        if (body.containsKey("lastSubsidyDate"))
            c.setLastSubsidyDate(LocalDate.parse((String)body.get("lastSubsidyDate")));
        svc.updateOnly(c);
        return ResponseEntity.ok(Map.of("message","ভর্তুকি আপডেট হয়েছে।"));
    }

    @PutMapping("/verify-land/{id}")
    public ResponseEntity<Object> verifyLand(@PathVariable int id, @RequestBody Map<String,String> body) {
        boolean verify = !"false".equalsIgnoreCase(body.getOrDefault("verify","true"));
        if (verify) svc.verifyLand(id, body.getOrDefault("officer","Admin"));
        else        svc.unverifyLand(id);
        return ResponseEntity.ok(Map.of("message", verify ? "জমি যাচাই হয়েছে।" : "যাচাই বাতিল।"));
    }

    @PutMapping("/assign/{id}")
    public ResponseEntity<Object> assign(@PathVariable int id, @RequestBody Map<String,String> body) {
        String officer = body.get("officer");
        if (officer == null || officer.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message","officer আবশ্যক।"));
        svc.assignOfficer(id, officer);
        return ResponseEntity.ok(Map.of("message","Officer assign হয়েছে।"));
    }

    @PutMapping("/renew/{id}")
    public ResponseEntity<Object> renew(@PathVariable int id) {
        FarmerCard c = svc.getById(id);
        if (c == null) return ResponseEntity.notFound().build();
        if (!"Approved".equalsIgnoreCase(c.getStatus()))
            return ResponseEntity.badRequest().body(Map.of("message","শুধুমাত্র Approved card renewal করা যাবে।"));
        svc.renew(id);
        return ResponseEntity.ok(Map.of("message","কার্ড ১ বছর নবায়ন হয়েছে।",
                "expireDate", svc.getById(id).getExpireDate().toString()));
    }

    @PutMapping("/bulk-approve")
    public ResponseEntity<Object> bulkApprove(@RequestBody Map<String,String> body) {
        String ward = body.get("ward");
        if (ward == null || ward.isBlank())
            return ResponseEntity.badRequest().body(Map.of("message","ward আবশ্যক।"));
        return ResponseEntity.ok(svc.bulkApproveByWard(ward, body.getOrDefault("approvedBy","Admin")));
    }

    @GetMapping("/lookup-by-cardno/{cardNo}")
    public ResponseEntity<Object> lookupByCardNo(@PathVariable String cardNo) {
      FarmerCard c = svc.getByCardNo(cardNo);   // add getByCardNo() to service+DAO
      if (c == null) return ResponseEntity.notFound().build();
      return ResponseEntity.ok(Map.of(
          "id",         c.getId(),
          "cardNo",     nvl(c.getCardNo()),
          "farmerName", nvl(c.getFarmerName()),
          "status",     nvl(c.getStatus()),
          "ward",       nvl(c.getWard()),
          "landTotal",  c.getLandTotal(),
          "landVerified", Boolean.TRUE.equals(c.getLandVerified()),
          "fertilizerQuota", c.getFertilizerQuota() != null ? c.getFertilizerQuota() : 0,
          "seedQuota",       c.getSeedQuota()       != null ? c.getSeedQuota()       : 0
      ));
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadCard(@PathVariable int id) {
        FarmerCard c = svc.getById(id);
        if (c == null) return ResponseEntity.notFound().build();
        byte[] pdf = pdfService.generateCard(c);
        return ResponseEntity.ok()
                .header("Content-Disposition","attachment; filename=\"FarmerCard_"+c.getCardNo()+".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable int id) {
        if (svc.getById(id) == null) return ResponseEntity.notFound().build();
        svc.delete(id);
        return ResponseEntity.ok(Map.of("message","মুছে ফেলা হয়েছে।"));
    }

    private String saveFile(MultipartFile f, String prefix, Path dir) throws IOException {
        String name = prefix + UUID.randomUUID() + "." + ext(f.getOriginalFilename());
        Files.copy(f.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        return "uploads/" + name;
    }
    private String ext(String n)  { return (n!=null&&n.contains(".")) ? n.substring(n.lastIndexOf('.')+1).toLowerCase() : "bin"; }
    private BigDecimal dec(String s) { try { return new BigDecimal(s); } catch (Exception e) { return BigDecimal.ZERO; } }
    private String nvl(String s)  { return s!=null?s:""; }
}
