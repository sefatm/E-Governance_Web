package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.ElectionDAO;
import com.mgt.model.Election;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ElectionService {

    @Autowired
    private ElectionDAO repo;

    private static final List<String> VALID_STATUSES =
            List.of("UPCOMING", "ACTIVE", "COMPLETED");

    public Election save(Election election) {
        if (election.getName() == null || election.getName().isBlank())
            throw new RuntimeException("Election-এর নাম দেওয়া আবশ্যক।");
        if (election.getType() == null || election.getType().isBlank())
            throw new RuntimeException("Election type দেওয়া আবশ্যক।");
        if (election.getStartDate() == null || election.getStartDate().isBlank())
            throw new RuntimeException("Start date দেওয়া আবশ্যক।");
        if (election.getEndDate() == null || election.getEndDate().isBlank())
            throw new RuntimeException("End date দেওয়া আবশ্যক।");
        if (election.getEndDate().compareTo(election.getStartDate()) < 0)
            throw new RuntimeException("End date, Start date-এর আগে হতে পারে না।");

        // FIX: Status সবসময় UPPERCASE-এ save হবে
        if (election.getStatus() == null || election.getStatus().isBlank()) {
            election.setStatus("UPCOMING");
        } else {
            election.setStatus(election.getStatus().toUpperCase());
        }

        return repo.save(election);
    }

    public List<Election> getAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public Election getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Election not found"));
    }

    public Election updateStatus(Integer id, String status) {
        if (status == null || !VALID_STATUSES.contains(status.toUpperCase()))
            throw new RuntimeException("Invalid status। শুধু UPCOMING, ACTIVE বা COMPLETED গ্রহণযোগ্য।");
        Election election = getById(id);
        // FIX: UPPERCASE enforce
        election.setStatus(status.toUpperCase());
        return repo.save(election);
    }

    public void delete(Integer id) {
        Election election = getById(id);
        if ("ACTIVE".equalsIgnoreCase(election.getStatus()))
            throw new RuntimeException("চলমান (ACTIVE) election delete করা যাবে না।");
        repo.deleteById(id);
    }
}
