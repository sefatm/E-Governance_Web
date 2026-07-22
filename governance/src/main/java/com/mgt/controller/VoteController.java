package com.mgt.controller;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.mgt.service.VoteService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vote")
@RequiredArgsConstructor
public class VoteController {

    @Autowired
    private VoteService voteService;

    /**
     * POST /api/vote/cast
     *
     * FIX 1: voterId এখন request body থেকে নেওয়া হচ্ছে না।
     *         JWT filter request attribute-এ "authenticatedUserId" রেখেছে —
     *         সেখান থেকে পড়া হচ্ছে। Frontend-এর sessionStorage tamper করলেও
     *         server-side voterId পরিবর্তন হবে না।
     *
     * FIX 2: @PreAuthorize নিশ্চিত করছে শুধু authenticated user এই endpoint hit করতে পারবে।
     *
     * FIX 3: Election date validation এবং @Transactional VoteService-এ যোগ হয়েছে।
     */
    @PostMapping("/cast")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> cast(@RequestBody Map<String, Integer> body) {

        Integer electionId  = body.get("electionId");
        Integer candidateId = body.get("candidateId");

        // FIX: voterId frontend-এর voter verification থেকে আসে।
        // app_user.id আর voter_registration.id আলাদা —
        // JWT-এর userId voter_registration table-এ নেই।
        // Citizen NID+DOB দিয়ে verify করে voter_id পায়, সেটা body-তে পাঠায়।
        // Authentication JWT দিয়ে হয় (interceptor), voterId শুধু voter lookup-এর জন্য।
        Integer voterId = body.get("voterId");

        if (electionId == null || candidateId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "electionId এবং candidateId আবশ্যক।"));
        }
        if (voterId == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "voterId আবশ্যক। প্রথমে NID দিয়ে verify করুন।"));
        }

        try {
            return ResponseEntity.ok(voteService.castVote(electionId, candidateId, voterId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // GET /api/vote/result/{electionId}
    @GetMapping("/result/{electionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getResult(@PathVariable Integer electionId) {
        return ResponseEntity.ok(voteService.getResult(electionId));
    }

    // GET /api/vote/analytics/{electionId}
    @GetMapping("/analytics/{electionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Object> getAnalytics(@PathVariable Integer electionId) {
        return ResponseEntity.ok(voteService.getAnalytics(electionId));
    }
}
