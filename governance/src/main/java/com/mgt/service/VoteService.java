package com.mgt.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import com.mgt.dao.AuditLogDAO;
import com.mgt.dao.ElectionDAO;
import com.mgt.dao.NomineeDAO;
import com.mgt.dao.VoteDAO;
import com.mgt.dao.VoterRegistrationDAO;
import com.mgt.model.AuditLog;
import com.mgt.model.Election;
import com.mgt.model.Nominee;
import com.mgt.model.Vote;
import com.mgt.model.VoterRegistration;

@Service
public class VoteService {

    @Autowired 
    private VoteDAO              voteDAO;
    
    @Autowired 
    private NomineeDAO           nomineeDAO;
    
    @Autowired 
    private VoterRegistrationDAO voterDAO;
    
    @Autowired 
    private ElectionDAO          electionDAO;
    
    @Autowired 
    private AuditLogDAO          auditLogDAO;
    
    @Autowired 
    private ApplicationEmailNotifier emailNotifier;

    // ── CAST VOTE ────────────────────────────────────────────────────────────
    @Transactional
    public Vote castVote(Integer electionId, Integer candidateId, Integer voterId) {

        // ── Election check ──────────────────────────────────────────────────
        Election election = electionDAO.findById(electionId)
                .orElseThrow(() -> new RuntimeException("Election পাওয়া যায়নি: id=" + electionId));

        if (!"ACTIVE".equalsIgnoreCase(election.getStatus())) {
            throw new RuntimeException("এই election টি সক্রিয় নয়।");
        }

        // FIX: Date range validation
        validateElectionDate(election);

        // ── Duplicate vote check ─────────────────────────────────────────────
        if (voteDAO.existsByVoter_IdAndElection_Id(voterId, electionId)) {
            throw new RuntimeException("আপনি ইতোমধ্যে এই election-এ ভোট দিয়েছেন।");
        }

        // ── Candidate check ──────────────────────────────────────────────────
        Nominee candidate = nomineeDAO.findById(candidateId)
                .orElseThrow(() -> new RuntimeException("Candidate পাওয়া যায়নি: id=" + candidateId));

        if (!"Approved".equalsIgnoreCase(candidate.getStatus())) {
            throw new RuntimeException("এই candidate অনুমোদিত নয়।");
        }

        // ── Voter check ──────────────────────────────────────────────────────
        VoterRegistration voter = voterDAO.findById(voterId)
                .orElseThrow(() -> new RuntimeException("Voter পাওয়া যায়নি: id=" + voterId));

        if (!"APPROVED".equalsIgnoreCase(voter.getStatus())) {
            throw new RuntimeException("আপনার voter registration অনুমোদিত নয়।");
        }

        // ── Election type must match voter's registered type ─────────────────
        if (election.getType() != null && voter.getElectionType() != null
                && !election.getType().equalsIgnoreCase(voter.getElectionType())) {
            throw new RuntimeException("আপনি এই ধরনের election-এ ভোট দিতে নিবন্ধিত নন।");
        }

        // ── Save vote ────────────────────────────────────────────────────────
        Vote newVote = new Vote();
        newVote.setElection(election);
        newVote.setCandidate(candidate);
        newVote.setVoter(voter);
        newVote.setStatus("Casted");

        Vote saved = voteDAO.save(newVote);

        saveAudit("VOTE_CAST", voter.getNid(), electionId,
                "Vote cast for candidate_id=" + candidateId);

        sendVotingCompleteEmail(voter, election, candidate);

        return saved;
    }

    // ── RESULT ───────────────────────────────────────────────────────────────
    public List<Map<String, Object>> getResult(Integer electionId) {
        List<Object[]> rows  = voteDAO.getVoteCountByElection(electionId);
        long           total = voteDAO.countByElection_Id(electionId);
        List<Map<String, Object>> results = new ArrayList<>();

        for (Object[] row : rows) {
            Integer cid   = (Integer) row[0];
            long    votes = ((Number) row[1]).longValue();
            double  pct   = total > 0 ? Math.round(votes * 1000.0 / total) / 10.0 : 0;
            nomineeDAO.findById(cid).ifPresent(n -> {
                Map<String, Object> item = new HashMap<>();
                item.put("candidateId",   cid);
                item.put("name",          n.getName());
                item.put("party",         n.getParty());
                item.put("symbol",        n.getSymbol());
                item.put("symbolFileUrl", n.getSymbolFileUrl());
                item.put("votes",         votes);
                item.put("percent",       pct);
                results.add(item);
            });
        }
        return results;
    }

    // ── ANALYTICS ────────────────────────────────────────────────────────────
    public Map<String, Object> getAnalytics(Integer electionId) {
        List<Map<String, Object>> results = getResult(electionId);
        long totalVotes = voteDAO.countByElection_Id(electionId);

        long totalApprovedVoters;
        long totalCandidates;
        try {
            Election election   = electionDAO.findById(electionId).orElseThrow();
            String electionType = election.getType();
            if (electionType != null && !electionType.isBlank()) {
                totalApprovedVoters = voterDAO.findByStatusAndElectionType("APPROVED", electionType).size();
                totalCandidates     = nomineeDAO.findByStatusAndElectionType("Approved", electionType).size();
            } else {
                totalApprovedVoters = voterDAO.findByStatus("APPROVED").size();
                totalCandidates     = nomineeDAO.findByStatus("Approved").size();
            }
        } catch (Exception e) {
            totalApprovedVoters = voterDAO.findByStatus("APPROVED").size();
            totalCandidates     = nomineeDAO.findByStatus("Approved").size();
        }

        double turnout = totalApprovedVoters > 0
                ? Math.round(totalVotes * 1000.0 / totalApprovedVoters) / 10.0 : 0;

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalVotes",          totalVotes);
        analytics.put("totalCandidates",     totalCandidates);
        analytics.put("totalApprovedVoters", totalApprovedVoters);
        analytics.put("turnoutPercent",      turnout);
        analytics.put("results",             results);
        return analytics;
    }

    // ── AUDIT LOGS ────────────────────────────────────────────────────────────
    public List<AuditLog> getAuditLogs() {
        return auditLogDAO.findAllByOrderByCreatedAtDesc();
    }

    // ── PRIVATE HELPERS ───────────────────────────────────────────────────────
    private void validateElectionDate(Election election) {
        try {
            LocalDate today = LocalDate.now();

            if (election.getStartDate() != null && !election.getStartDate().isBlank()) {
                LocalDate start = LocalDate.parse(election.getStartDate().substring(0, 10));
                if (today.isBefore(start)) {
                    throw new RuntimeException("ভোটগ্রহণ এখনও শুরু হয়নি। শুরুর তারিখ: " + election.getStartDate());
                }
            }

            if (election.getEndDate() != null && !election.getEndDate().isBlank()) {
                LocalDate end = LocalDate.parse(election.getEndDate().substring(0, 10));
                if (today.isAfter(end)) {
                    throw new RuntimeException("ভোটগ্রহণের সময়সীমা শেষ হয়েছে। শেষ তারিখ ছিল: " + election.getEndDate());
                }
            }
        } catch (DateTimeParseException e) {
            // Date parse করতে না পারলে block করব না — log করব
            System.err.println("Election date parse error for id=" + election.getId() + ": " + e.getMessage());
        }
    }

    private void sendVotingCompleteEmail(VoterRegistration voter,
                                         Election election,
                                         Nominee candidate) {
        String email = voter.getEmail();
        if (email == null || email.isBlank() || !email.contains("@")) return;

        String subject = "✅ আপনার ভোট সফলভাবে প্রদান হয়েছে – " + election.getName();
        String body    = buildVoteConfirmationEmail(
                voter.getName(), election.getName(),
                election.getType(), candidate.getName(), candidate.getParty()
        );
        emailNotifier.sendHtml(email, subject, body);
    }

    private String buildVoteConfirmationEmail(String voterName, String electionName,
                                               String electionType, String candidateName,
                                               String party) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
             + "<body style='font-family:\"Segoe UI\",Arial,sans-serif;background:#f1f5f9;margin:0;padding:20px'>"
             + "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;"
             + "overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)'>"
             + "<div style='background:linear-gradient(135deg,#064e3b,#065f46);padding:28px;"
             + "border-bottom:4px solid #f59e0b'>"
             + "<h2 style='color:#fff;margin:0;font-size:20px'>গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>"
             + "<p style='color:#a7f3d0;margin:6px 0 0;font-size:13px'>E-Voting Portal</p></div>"
             + "<div style='padding:28px'>"
             + "<p style='color:#374151;font-size:15px'>প্রিয় <strong>" + voterName + " অভিনন্দন </strong>,</p>"
             + "<p style='color:#374151;line-height:1.8'>আপনার ভোট নিরাপদে রেকর্ড করা হয়েছে।</p>"
             + "<table style='width:100%;border-collapse:collapse;background:#f8fafc;border-radius:8px;padding:16px'>"
             + "<tr><td style='color:#64748b;font-size:13px;padding:6px 8px'>নির্বাচন</td>"
             + "<td style='font-weight:600;font-size:13px'>" + electionName + "</td></tr>"
             + "<tr><td style='color:#64748b;font-size:13px;padding:6px 8px'>ধরন</td>"
             + "<td style='font-weight:600;font-size:13px'>" + (electionType != null ? electionType : "—") + "</td></tr>"
//             + "<tr><td style='color:#64748b;font-size:13px;padding:6px 8px'>প্রার্থী</td>"
//             + "<td style='font-weight:600;font-size:13px'>" + candidateName + "</td></tr>"
//             + "<tr><td style='color:#64748b;font-size:13px;padding:6px 8px'>দল</td>"
//             + "<td style='font-weight:600;font-size:13px'>" + (party != null ? party : "—") + "</td></tr>"
             + "</table></div>"
             + "<div style='background:#f8fafc;padding:16px;border-top:1px solid #e2e8f0;text-align:center'>"
             + "<p style='color:#94a3b8;font-size:11px;margin:0'>E-Voting Portal | গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</p>"
             + "</div></div></body></html>";
    }

    private void saveAudit(String action, String nid, Integer electionId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setNid(nid);
        log.setElectionId(electionId);
        log.setDetails(details);
        log.setModule("E-Voting");
        log.setStatus("Success");

        // FIX: username NOT NULL constraint পূরণ — NID দিয়ে identify, না থাকলে SYSTEM
        log.setUsername(nid != null && !nid.isBlank() ? nid : "SYSTEM");

        // FIX: voter role সেট করো
        log.setUserRole("Citizen");

        // IP address — request context থেকে নাও
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String ip = req.getHeader("X-Forwarded-For");
                if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
                log.setIpAddress(ip);
            }
        } catch (Exception ignored) {
            // request context না থাকলে IP ছাড়াই save করো
        }

        auditLogDAO.save(log);
    }
}
