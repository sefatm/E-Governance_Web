package com.mgt.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mgt.model.AppUser;
import com.mgt.security.JwtUtil;           // ← NEW
import com.mgt.service.PasswordResetService;
import com.mgt.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired UserService userService;
    @Autowired PasswordResetService passwordResetService;
    @Autowired JwtUtil jwtUtil;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static final String UPLOAD_DIR = "src/main/resources/uploads/";

    // ── REGISTER ────────────────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<Object> register(@RequestBody AppUser user) {
        if (user.getName() == null || user.getName().isBlank())
            return bad("Name required");
        if (user.getEmail() == null || user.getEmail().isBlank())
            return bad("Email required");
        if (user.getPassword() == null || user.getPassword().length() < 6)
            return bad("Password must be at least 6 characters");

        String error = userService.register(user);
        if (error != null) return bad(error);
        return ok("Registration successful");
    }

    // ── LOGIN ────────────────────────────────────────────────────────────────
    // FIX: "token_" + id এর পরিবর্তে real JWT token return করছে
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String password = body.get("password");

        if (email == null || password == null)
            return bad("Email & password required");

        AppUser user = userService.findByEmail(email);
        if (user == null)                                return unauthorized("Invalid credentials");
        if ("Pending".equalsIgnoreCase(user.getStatus()))   return forbidden("Account pending");
        if ("Inactive".equalsIgnoreCase(user.getStatus()))  return forbidden("Account inactive");

        AppUser loginUser = userService.login(email, password);
        if (loginUser == null) return unauthorized("Invalid credentials");

        // ── Access Token (2 hours) + Refresh Token (7 days) ──────────────
        String accessToken  = jwtUtil.generateAccessToken(
            loginUser.getId(), loginUser.getEmail(), loginUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(
            loginUser.getId(), loginUser.getEmail());

        return ResponseEntity.ok(Map.of(
            "id",           loginUser.getId(),
            "name",         loginUser.getName(),
            "email",        loginUser.getEmail(),
            "role",         loginUser.getRole(),
            "status",       loginUser.getStatus(),
            "photoUrl",     loginUser.getPhotoUrl() == null ? "" : loginUser.getPhotoUrl(),
            "token",        accessToken,
            "refreshToken", refreshToken,
            "expiresIn",    jwtUtil.getAccessExpiryMs() / 1000   // seconds
        ));
    }

    // ── REFRESH TOKEN ────────────────────────────────────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank())
            return bad("Refresh token required.");

        if (!jwtUtil.isRefreshTokenValid(refreshToken))
            return ResponseEntity.status(401).body(Map.of(
                "code", "REFRESH_EXPIRED",
                "message", "Refresh token expired. Please login again."));

        int userId = jwtUtil.getUserIdFromRefresh(refreshToken);
        AppUser user = userService.getById(userId);
        if (user == null)                                   return unauthorized("User not found.");
        if ("Inactive".equalsIgnoreCase(user.getStatus())) return forbidden("Account inactive.");
        if ("Pending".equalsIgnoreCase(user.getStatus()))  return forbidden("Account pending.");

        String newAccessToken  = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(Map.of(
            "token",        newAccessToken,
            "refreshToken", newRefreshToken,
            "expiresIn",    jwtUtil.getAccessExpiryMs() / 1000
        ));
    }

    // ── FORGOT PASSWORD: Send OTP ────────────────────────────────────────────
    @PostMapping("/forgot-password/send-otp")
    public ResponseEntity<Object> sendOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) return bad("Email required");
        passwordResetService.sendOtp(email);
        return ok("OTP sent to email");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Object> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.isBlank()) return bad("Email required");
        passwordResetService.initiateReset(email);
        return ok("If the email exists, a reset link has been sent.");
    }

 // ── FORGOT PASSWORD: Verify OTP
    @PostMapping("/forgot-password/verify-otp")
    public ResponseEntity<Object> verifyOtp(@RequestBody Map<String, String> body) {

        String email = body.get("email");
        String otp = body.get("otp");

        if (email == null || otp == null) {
            return bad("Email and OTP required");
        }

        String error = passwordResetService.verifyOtp(email, otp);

        if (error != null) {
            return bad(error);
        }

        return ok("OTP verified");
    }

    // ── RESET PASSWORD ───────────────────────────────────────────────────────

    // ── FORGOT PASSWORD: Reset via OTP (Step 3) ────────────────────────────────────────
    @PostMapping("/forgot-password/reset")
    public ResponseEntity<Object> resetViaOtp(@RequestBody Map<String, String> body) {
        String email    = body.get("email");
        String otp      = body.get("otp");
        String password = body.get("password");
        if (email == null || otp == null || password == null)
            return bad("Email, OTP ও password আবশ্যক।");
        if (password.length() < 6)
            return bad("Password কমপক্ষে ৬ অক্ষরের হতে হবে।");
        String error = passwordResetService.resetWithOtp(email, otp, password);
        if (error != null) return bad(error);
        return ok("পাসওয়ার্ড সফলভাবে পরিবর্তন হয়েছে।");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Object> resetPassword(@RequestBody Map<String, String> body) {
        String token    = body.get("token");
        String password = body.get("password");
        if (token == null || password == null) return bad("Token and password required");
        if (password.length() < 6) return bad("Password must be at least 6 characters");
        String error = passwordResetService.resetPassword(token, password);
        if (error != null) return bad(error);
        return ok("Password reset successful");
    }

    @GetMapping("/reset-password/validate")
    public ResponseEntity<Object> validateResetToken(@RequestParam("token") String token) {
        String error = passwordResetService.validateToken(token);
        if (error != null) return bad(error);
        return ok("Reset token is valid");
    }

    // ── UPLOAD PHOTO ─────────────────────────────────────────────────────────
    // ── Legacy endpoint (backward compatibility)
    @PostMapping(value = "/upload-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadPhoto(@RequestParam("file") MultipartFile file,
                                              @RequestParam("userId") int userId) {
        return handlePhotoUpload(file, userId);
    }

    // ── New endpoint: POST /api/auth/profile/{id}/photo  (param: photo)
    @PostMapping(value = "/profile/{id}/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> uploadProfilePhoto(@PathVariable int id,
                                                     @RequestParam("photo") MultipartFile file) {
        return handlePhotoUpload(file, id);
    }

    private ResponseEntity<Object> handlePhotoUpload(MultipartFile file, int userId) {
        Set<String> allowed = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");
        if (!allowed.contains(file.getContentType()))
            return bad("Only JPG, PNG, WEBP, GIF images are allowed.");

        if (file.getSize() > 5 * 1024 * 1024)
            return bad("File size must be less than 5MB.");

        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            String orig = file.getOriginalFilename();
            String ext  = (orig != null && orig.contains("."))
                          ? orig.substring(orig.lastIndexOf('.')) : ".jpg";
            String filename   = "profile_" + userId + "_" + UUID.randomUUID() + ext;
            Path   targetPath = Paths.get(UPLOAD_DIR + filename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            String photoUrl = "uploads/" + filename;
            userService.updatePhoto(userId, photoUrl);
            return ResponseEntity.ok(Map.of("photoUrl", photoUrl));
        } catch (IOException e) {
            return bad("File upload failed: " + e.getMessage());
        }
    }
    
    // ── PUT /api/auth/profile/{id} — update name & email
    @PutMapping("/profile/{id}")
    public ResponseEntity<?> updateProfile(@PathVariable int id,
                                           @RequestBody Map<String, String> body) {
        AppUser existing = userService.getById(id);
        if (existing == null) return ResponseEntity.notFound().build();

        String name  = body.getOrDefault("name",  existing.getName());
        String email = body.getOrDefault("email", existing.getEmail());

        userService.updateProfile(id, name, email);
        AppUser updated = userService.getById(id);
        return ResponseEntity.ok(updated);
    }

    // ── PUT /api/auth/change-password/{id}
    @PutMapping("/change-password/{id}")
    public ResponseEntity<?> changePassword(@PathVariable int id,
                                            @RequestBody Map<String, String> body) {
        AppUser user = userService.getById(id);
        if (user == null) return ResponseEntity.notFound().build();

        String currentPw = body.get("currentPassword");
        String newPw     = body.get("newPassword");

        if (currentPw == null || newPw == null)
            return bad("currentPassword and newPassword are required.");

        if (!passwordEncoder.matches(currentPw, user.getPassword()))
            return bad("Current password is incorrect.");

        if (newPw.length() < 6)
            return bad("New password must be at least 6 characters.");

        userService.changePassword(id, newPw);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getProfile(@PathVariable int id) {

        AppUser user = userService.getById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────
    private ResponseEntity<Object> ok(String msg) {
        return ResponseEntity.ok(Map.of("message", msg));
    }
    private ResponseEntity<Object> bad(String msg) {
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }
    private ResponseEntity<Object> unauthorized(String msg) {
        return ResponseEntity.status(401).body(Map.of("message", msg));
    }
    private ResponseEntity<Object> forbidden(String msg) {
        return ResponseEntity.status(403).body(Map.of("message", msg));
    }
}
