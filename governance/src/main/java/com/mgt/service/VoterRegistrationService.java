package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.AuditLogDAO;
import com.mgt.dao.VoteDAO;
import com.mgt.dao.VoterRegistrationDAO;
import com.mgt.model.AuditLog;
import com.mgt.model.VoterRegistration;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoterRegistrationService {

    @Autowired private VoterRegistrationDAO voterDAO;
    @Autowired private VoteDAO              voteDAO;
    @Autowired private AuditLogDAO          auditLogDAO;
    @Autowired private ApplicationEmailNotifier emailNotifier;

    // Register voter
    public VoterRegistration save(VoterRegistration voter) {
        if (voter.getNid() == null || voter.getNid().isBlank()) {
            throw new RuntimeException("NID required");
        }
        if (voterDAO.existsByNid(voter.getNid())) {
            throw new RuntimeException("এই NID দিয়ে ইতোমধ্যে registration করা হয়েছে।");
        }
        voter.setStatus("PENDING");
        VoterRegistration saved = voterDAO.save(voter);
        saveAudit("VOTER_REGISTERED", voter.getNid(), null, "Voter registered: " + voter.getName());
        emailNotifier.sendApplicationReceived(
                voter.getMobile(), voter.getName(),
                "ভোটার নিবন্ধন", String.valueOf(saved.getId())
        );
        return saved;
    }

    // Get all voters
    public List<VoterRegistration> getAll() {
        return voterDAO.findAllByOrderByCreatedAtDesc();
    }

    // Approve voter
    public VoterRegistration approve(Integer id) {
        VoterRegistration voter = getById(id);
        voter.setStatus("APPROVED");
        VoterRegistration saved = voterDAO.save(voter);
        saveAudit("VOTER_APPROVED", voter.getNid(), null, "Voter approved: " + voter.getName());
        emailNotifier.sendStatusUpdate(
                voter.getMobile(), voter.getName(),
                "ভোটার নিবন্ধন", String.valueOf(id), "Approved", null
        );
        return saved;
    }

    // Reject voter
    public VoterRegistration reject(Integer id, String reason) {
        VoterRegistration voter = getById(id);
        voter.setStatus("REJECTED");
        voter.setRejectReason(reason);
        VoterRegistration saved = voterDAO.save(voter);
        saveAudit("VOTER_REJECTED", voter.getNid(), null,
                "Voter rejected: " + voter.getName() + " | Reason: " + reason);
        emailNotifier.sendStatusUpdate(
                voter.getMobile(), voter.getName(),
                "ভোটার নিবন্ধন", String.valueOf(id), "Rejected", reason
        );
        return saved;
    }

    /**
     * FIX: delete করার আগে vote cast করেছে কিনা check করা হচ্ছে।
     * আগে সরাসরি deleteById করা হত, ফলে vote_cast table-এ FK constraint error আসত।
     */
    public void delete(Integer id) {
        VoterRegistration voter = getById(id);
        long voteCount = voteDAO.countByVoter_Id(id);
        if (voteCount > 0) {
            throw new RuntimeException(
                "এই voter ইতোমধ্যে " + voteCount + "টি ভোট দিয়েছেন। " +
                "ভোট দেওয়া voter-কে delete করা যাবে না।"
            );
        }
        voterDAO.deleteById(id);
        saveAudit("VOTER_DELETED", voter.getNid(), null, "Voter deleted: " + voter.getName());
    }

    // Verify voter by NID + DOB
    public VoterRegistration verify(String nid, String dob) {
        VoterRegistration voter = voterDAO.findByNidAndDob(nid, dob)
                .orElseThrow(() -> new RuntimeException("NID ও জন্ম তারিখ মিলছে না।"));
        if (!"APPROVED".equals(voter.getStatus())) {
            throw new RuntimeException("আপনার voter registration অনুমোদিত নয়।");
        }
        return voter;
    }

    // Check voting status
    public boolean hasVoted(Integer voterId, Integer electionId) {
        return voteDAO.existsByVoter_IdAndElection_Id(voterId, electionId);
    }

    private VoterRegistration getById(Integer id) {
        return voterDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Voter not found"));
    }

    private void saveAudit(String action, String nid, Integer electionId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setNid(nid);
        log.setElectionId(electionId);
        log.setDetails(details);
        log.setModule("E-Voting");
        log.setStatus("Success");
        // FIX: username NOT NULL constraint পূরণ
        log.setUsername(nid != null && !nid.isBlank() ? nid : "SYSTEM");
        log.setUserRole("Citizen");
        auditLogDAO.save(log);
    }
}
