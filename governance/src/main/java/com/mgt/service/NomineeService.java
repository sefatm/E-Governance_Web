package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.mgt.dao.AuditLogDAO;
import com.mgt.dao.ElectionDAO;
import com.mgt.dao.NomineeDAO;
import com.mgt.model.AuditLog;
import com.mgt.model.Election;
import com.mgt.model.Nominee;

@Service
public class NomineeService {

    @Autowired
    private NomineeDAO nomineeRepository;

    @Autowired
    private AuditLogDAO auditLogDAO;

    @Autowired
    private ElectionDAO electionDAO;

    @Autowired
    private ApplicationEmailNotifier emailNotifier;

    public Nominee save(Nominee nominee) {
        if (nominee.getElectionType() == null || nominee.getElectionType().isBlank()) {
            throw new RuntimeException("Upcoming election নির্বাচন করা আবশ্যক।");
        }
        boolean hasUpcomingElection = !electionDAO
                .findByStatusAndType("UPCOMING", nominee.getElectionType())
                .isEmpty();
        if (!hasUpcomingElection) {
            throw new RuntimeException("এই election type-এর জন্য কোনো UPCOMING election নেই। Candidate apply এখন বন্ধ।");
        }
        nominee.setStatus("PENDING");
        Nominee saved = nomineeRepository.save(nominee);
        saveAudit("NOMINATION_SUBMITTED", nominee.getNid(), null,
                "Nomination: " + nominee.getName() + " | Party: " + nominee.getParty());

        // আবেদন জমার notification
        emailNotifier.sendApplicationReceived(
                nominee.getMobileNumber(),
                nominee.getName(),
                "প্রার্থিতা মনোনয়ন",
                String.valueOf(saved.getId())
        );
        return saved;
    }

    public List<Nominee> getAll() {
        return nomineeRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Nominee> getApproved(String electionType) {
        return nomineeRepository.findByStatusAndElectionType("APPROVED", electionType);
    }

    public List<Nominee> getApprovedForElection(Integer electionId) {
        Election election = electionDAO.findById(electionId)
                .orElseThrow(() -> new RuntimeException("Election not found"));
        if (election.getType() == null || election.getType().isBlank()) {
            return getAllApproved();
        }
        return getApproved(election.getType());
    }

    public List<Nominee> getAllApproved() {
        return nomineeRepository.findByStatus("APPROVED");
    }

    public Nominee approve(Integer id) {
        Nominee nominee = getById(id);
        nominee.setStatus("APPROVED");
        Nominee saved = nomineeRepository.save(nominee);
        saveAudit("CANDIDATE_APPROVED", nominee.getNid(), null, "Candidate approved: " + nominee.getName());

        // Approve notification
        emailNotifier.sendStatusUpdate(
                nominee.getMobileNumber(), nominee.getName(),
                "প্রার্থিতা মনোনয়ন", String.valueOf(id),
                "Approved", null
        );
        return saved;
    }

    public Nominee reject(Integer id, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new RuntimeException("Rejection-এর কারণ দেওয়া আবশ্যক।");
        }
        Nominee nominee = getById(id);
        nominee.setStatus("REJECTED");
        nominee.setRejectReason(reason);
        Nominee saved = nomineeRepository.save(nominee);
        saveAudit("CANDIDATE_REJECTED", nominee.getNid(), null,
                "Candidate rejected: " + nominee.getName() + " | Reason: " + reason);

        // Reject notification
        emailNotifier.sendStatusUpdate(
                nominee.getMobileNumber(), nominee.getName(),
                "প্রার্থিতা মনোনয়ন", String.valueOf(id),
                "Rejected", reason
        );
        return saved;
    }

    public void delete(Integer id) {
        nomineeRepository.deleteById(id);
    }

    private Nominee getById(Integer id) {
        return nomineeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nominee not found"));
    }

    private void saveAudit(String action, String nid, Integer electionId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setNid(nid);
        log.setElectionId(electionId);
        log.setDetails(details);
        log.setModule("E-Voting");
        log.setStatus("Success");
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String principal = String.valueOf(auth.getPrincipal());
                log.setUsername(principal.equals("anonymousUser") ? "System" : principal);
                auth.getAuthorities().stream().findFirst().ifPresent(a ->
                    log.setUserRole(a.getAuthority().replace("ROLE_", "")));
            } else {
                log.setUsername("System");
                log.setUserRole("System");
            }
        } catch (Exception e) {
            log.setUsername("System");
            log.setUserRole("System");
        }
        auditLogDAO.save(log);
    }
}
