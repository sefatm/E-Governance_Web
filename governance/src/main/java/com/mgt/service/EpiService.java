package com.mgt.service;

import com.mgt.dao.EpiChildDAO;
import com.mgt.dao.EpiVaccinationDAO;
import com.mgt.model.EpiChild;
import com.mgt.model.EpiVaccination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class EpiService {

    @Autowired EpiChildDAO childDAO;
    @Autowired EpiVaccinationDAO vaccDAO;
    @Autowired ApplicationEmailNotifier emailNotifier;

    private static final DateTimeFormatter BD_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final Object[][] EPI_SCHEDULE = {
        { "BCG",   "1",  0   },
        { "OPV",   "0",  0   },
        { "Penta", "1",  42  },
        { "OPV",   "1",  42  },
        { "PCV",   "1",  42  },
        { "Penta", "2",  70  },
        { "OPV",   "2",  70  },
        { "PCV",   "2",  70  },
        { "Penta", "3",  98  },
        { "OPV",   "3",  98  },
        { "PCV",   "3",  98  },
        { "IPV",   "1",  98  },
        { "MR",    "1",  274 },
        { "MR",    "2",  457 },
    };

    // ── Register Child ───────────────────────────────────────
    @Transactional
    public EpiChild register(EpiChild child) {
        String cardNo = "EPI-" + LocalDate.now().getYear()
                      + "-" + String.format("%05d", (int)(Math.random() * 99999 + 1));
        child.setCardNo(cardNo);
        child.setStatus("Pending");
        child.setApprovalStage(0);
        EpiChild saved = childDAO.save(child);
        generateSchedule(saved);

        return saved;
    }

    private void generateSchedule(EpiChild child) {
        LocalDate dob = child.getDateOfBirth();
        List<EpiVaccination> list = new ArrayList<>();

        for (Object[] row : EPI_SCHEDULE) {
            String vaccine = (String) row[0];
            String dose    = (String) row[1];
            int    days    = (int)    row[2];

            if (!vaccDAO.findByChildAndVaccine(child.getId(), vaccine, dose).isEmpty()) continue;

            EpiVaccination v = new EpiVaccination();
            v.setChild(child);
            v.setVaccineName(vaccine);
            v.setDoseNo(dose);
            v.setScheduledDate(dob.plusDays(days));
            v.setStatus(dob.plusDays(days).isBefore(LocalDate.now()) ? "Due" : "Scheduled");
            list.add(v);
        }
        vaccDAO.saveAll(list);
    }

    // ── Mark Vaccine as Given ────────────────────────────────
    @Transactional
    public EpiVaccination markGiven(Integer vaccId, Map<String, String> body) {
        EpiVaccination v = vaccDAO.findById(vaccId)
                .orElseThrow(() -> new RuntimeException("Vaccination record not found"));

        EpiChild doseChild = v.getChild();
        if (doseChild == null || !"Approved".equalsIgnoreCase(doseChild.getStatus())) {
            throw new RuntimeException("EPI registration must be approved before recording a vaccine dose");
        }
        if ("Given".equalsIgnoreCase(v.getStatus())) {
            throw new RuntimeException("This vaccine dose has already been recorded as given");
        }

        v.setStatus("Given");
        v.setGivenDate(LocalDate.now());
        v.setGivenBy(body.getOrDefault("givenBy",       "Health Worker"));
        v.setHealthCenter(body.getOrDefault("healthCenter", ""));
        v.setBatchNo(body.getOrDefault("batchNo",       ""));
        v.setRemarks(body.getOrDefault("remarks",       ""));
        EpiVaccination saved = vaccDAO.save(v);

        // Dose given notification email to guardian
        EpiChild child = saved.getChild();
        if (child != null && child.getGuardianEmail() != null && !child.getGuardianEmail().isBlank()) {
            // Find next scheduled dose for this child
            EpiVaccination nextDose = vaccDAO
                .findByChild_IdOrderByScheduledDateAsc(child.getId())
                .stream()
                .filter(d -> "Scheduled".equals(d.getStatus()) || "Due".equals(d.getStatus()))
                .min(Comparator.comparing(EpiVaccination::getScheduledDate))
                .orElse(null);

            if (nextDose != null) {
                saved.setNextDueDate(nextDose.getScheduledDate());
                vaccDAO.save(saved);
            }

            String subject = "💉 টিকা দেওয়া হয়েছে — " + saved.getVaccineName()
                           + " (Dose " + saved.getDoseNo() + ") | " + child.getChildName();
            String html = buildVaccineDoseEmail(child, saved, nextDose);
            emailNotifier.sendHtml(child.getGuardianEmail(), subject, html);
        }

        return saved;
    }

    // ── Approve child registration ──────────────────────────────
    @Transactional
    public EpiChild approveChild(Integer id, Map<String, String> body) {
        EpiChild child = childDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Child not found: " + id));
        String signatureBase64 = body == null ? null : body.get("signatureBase64");
        String sealBase64 = body == null ? null : body.get("sealBase64");
        if (signatureBase64 == null || signatureBase64.isBlank()) {
            throw new IllegalArgumentException("Signature is required");
        }
        if (sealBase64 == null || sealBase64.isBlank()) {
            throw new IllegalArgumentException("Seal is required");
        }
        int stage = child.getApprovalStage() == null ? 0 : child.getApprovalStage();
        String approvedBy = body == null ? "Health Officer" : body.getOrDefault("approvedBy", "Health Officer");
        if (stage <= 0 || "Pending".equalsIgnoreCase(child.getStatus())) {
            child.setFirstSignature(signatureBase64);
            child.setFirstSeal(sealBase64);
            child.setFirstApprovedBy(approvedBy);
            child.setFirstApprovedAt(java.time.LocalDateTime.now());
            child.setApprovalStage(1);
            child.setStatus("First Approved");
        } else if (stage == 1 || "First Approved".equalsIgnoreCase(child.getStatus())) {
            child.setSecondSignature(signatureBase64);
            child.setSecondSeal(sealBase64);
            child.setSecondApprovedBy(approvedBy);
            child.setSecondApprovedAt(java.time.LocalDateTime.now());
            child.setAuthoritySignature(signatureBase64);
            child.setAuthoritySeal(sealBase64);
            child.setApprovalStage(2);
            child.setStatus("Approved");
        } else {
            throw new IllegalStateException("EPI registration is already fully approved");
        }
        EpiChild saved = childDAO.save(child);

        // Approval email with full schedule
        if ("Approved".equalsIgnoreCase(saved.getStatus()) && saved.getGuardianEmail() != null && !saved.getGuardianEmail().isBlank()) {
            String subject = "✅ EPI কার্ড অনুমোদিত হয়েছে — " + saved.getChildName();
            String html = buildApprovalEmail(saved);
            emailNotifier.sendHtml(saved.getGuardianEmail(), subject, html);
        }
        return saved;
    }

    public EpiChild approveChild(Integer id) {
        return approveChild(id, java.util.Collections.emptyMap());
    }

    // ── Mark as Missed ───────────────────────────────────────
    @Transactional
    public EpiVaccination markMissed(Integer vaccId) {
        EpiVaccination v = vaccDAO.findById(vaccId)
                .orElseThrow(() -> new RuntimeException("Not found"));
        v.setStatus("Missed");
        EpiVaccination saved = vaccDAO.save(v);

        // Missed dose alert email
        EpiChild child = saved.getChild();
        if (child != null && child.getGuardianEmail() != null && !child.getGuardianEmail().isBlank()) {
            String subject = "⚠️ টিকা মিস হয়েছে — " + saved.getVaccineName()
                           + " | " + child.getChildName();
            String html = buildMissedDoseEmail(child, saved);
            emailNotifier.sendHtml(child.getGuardianEmail(), subject, html);
        }

        return saved;
    }

    // ── Queries ──────────────────────────────────────────────
    public List<EpiChild> getAllChildren()                    { return childDAO.findAllByOrderByCreatedAtDesc(); }
    public List<EpiChild> getPendingChildren() {
        return childDAO.findAllByOrderByCreatedAtDesc().stream()
                .filter(c -> "Pending".equalsIgnoreCase(c.getStatus()) || "First Approved".equalsIgnoreCase(c.getStatus()))
                .toList();
    }
    public List<EpiChild> search(String query)               { return childDAO.search(query); }
    public EpiChild getChildById(Integer id)                 { return childDAO.findById(id).orElseThrow(() -> new RuntimeException("Child not found: " + id)); }
    public Optional<EpiChild> getByCardNo(String cardNo)     { return childDAO.findByCardNo(cardNo); }

    public List<Map<String, Object>> getVaccinationSchedule(Integer childId) {
        List<EpiVaccination> list = vaccDAO.findByChild_IdOrderByScheduledDateAsc(childId);
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (EpiVaccination v : list) {
            if ("Scheduled".equals(v.getStatus()) && v.getScheduledDate().isBefore(today)) {
                v.setStatus("Due"); vaccDAO.save(v);
            }
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id",            v.getId());
            map.put("vaccineName",   v.getVaccineName());
            map.put("doseNo",        v.getDoseNo());
            map.put("scheduledDate", v.getScheduledDate().toString());
            map.put("givenDate",     v.getGivenDate() != null ? v.getGivenDate().toString() : null);
            map.put("status",        v.getStatus());
            map.put("givenBy",       v.getGivenBy());
            map.put("healthCenter",  v.getHealthCenter());
            map.put("batchNo",       v.getBatchNo());
            map.put("remarks",       v.getRemarks());
            result.add(map);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        LocalDate today = LocalDate.now();
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalChildren",  childDAO.count());
        stats.put("totalGiven",     vaccDAO.countByStatus("Given"));
        stats.put("totalScheduled", vaccDAO.countByStatus("Scheduled"));
        stats.put("totalMissed",    vaccDAO.countByStatus("Missed"));
        stats.put("dueSoon",        vaccDAO.findUpcoming(today, today.plusDays(7)).size());
        stats.put("overdueCount",   vaccDAO.findMissed(today).size());
        return stats;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getUpcomingVaccinations() {
        LocalDate today = LocalDate.now();
        return vaccDAO.findUpcoming(today, today.plusDays(7)).stream().map(v -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("vaccinationId",  v.getId());
            m.put("childId",        v.getChild().getId());
            m.put("childName",      v.getChild().getChildName());
            m.put("cardNo",         v.getChild().getCardNo());
            m.put("guardianPhone",  v.getChild().getGuardianPhone());
            m.put("ward",           v.getChild().getWard());
            m.put("vaccineName",    v.getVaccineName());
            m.put("doseNo",         v.getDoseNo());
            m.put("scheduledDate",  v.getScheduledDate().toString());
            m.put("status",         v.getStatus());
            return m;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMissedVaccinations() {
        return vaccDAO.findMissed(LocalDate.now()).stream().map(v -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("vaccinationId",  v.getId());
            m.put("childId",        v.getChild().getId());
            m.put("childName",      v.getChild().getChildName());
            m.put("cardNo",         v.getChild().getCardNo());
            m.put("guardianPhone",  v.getChild().getGuardianPhone());
            m.put("ward",           v.getChild().getWard());
            m.put("vaccineName",    v.getVaccineName());
            m.put("doseNo",         v.getDoseNo());
            m.put("scheduledDate",  v.getScheduledDate().toString());
            return m;
        }).toList();
    }

    @Transactional
    public void deleteChild(Integer id) { childDAO.deleteById(id); }

    // ══════════════════════════════════════════════════════════
    // Email HTML templates
    // ══════════════════════════════════════════════════════════

    private String buildRegistrationEmail(EpiChild child) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:\"Segoe UI\",Arial,sans-serif;background:#f1f5f9;margin:0;padding:20px'>"
            + "<div style='max-width:620px;margin:0 auto;background:#fff;border-radius:14px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1)'>"
            + "<div style='background:linear-gradient(135deg,#064e3b,#065f46);padding:28px;border-bottom:4px solid #f59e0b'>"
            + "<h2 style='color:#fff;margin:0;font-size:20px'>গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>"
            + "<p style='color:#a7f3d0;margin:5px 0 0;font-size:13px'>স্বাস্থ্য বিভাগ — EPI টিকা কার্যক্রম</p>"
            + "</div>"
            + "<div style='padding:28px'>"
            + "<h3 style='color:#065f46;margin-top:0'>✅ EPI কার্ড সফলভাবে নিবন্ধিত হয়েছে</h3>"
            + "<p style='color:#374151'>প্রিয় অভিভাবক,</p>"
            + "<p style='color:#374151'>আপনার শিশুর EPI টিকা কার্ড সফলভাবে নিবন্ধিত হয়েছে।</p>"
            + "<div style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:10px;padding:16px 20px;margin:20px 0'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + row("কার্ড নম্বর",   child.getCardNo())
            + row("শিশুর নাম",     child.getChildName())
            + row("জন্ম তারিখ",    child.getDateOfBirth().format(BD_DATE))
            + row("লিঙ্গ",          child.getGender())
            + row("পিতার নাম",     child.getFatherName())
            + row("মাতার নাম",     child.getMotherName())
            + row("ওয়ার্ড",         child.getWard() != null ? child.getWard().toString() : "-")
            + "</table></div>"
            + "<p style='color:#6b7280;font-size:13px'>পরবর্তী টিকার তারিখ SMS এবং email এর মাধ্যমে জানানো হবে।</p>"
            + "</div>"
            + footer()
            + "</div></body></html>";
    }

    private String buildVaccineDoseEmail(EpiChild child, EpiVaccination given, EpiVaccination next) {
        String nextInfo = next != null
            ? next.getVaccineName() + " Dose-" + next.getDoseNo()
              + " — " + next.getScheduledDate().format(BD_DATE)
            : "সব টিকা সম্পন্ন হয়েছে 🎉";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:\"Segoe UI\",Arial,sans-serif;background:#f1f5f9;margin:0;padding:20px'>"
            + "<div style='max-width:620px;margin:0 auto;background:#fff;border-radius:14px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1)'>"
            + "<div style='background:linear-gradient(135deg,#064e3b,#065f46);padding:28px;border-bottom:4px solid #f59e0b'>"
            + "<h2 style='color:#fff;margin:0;font-size:20px'>গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>"
            + "<p style='color:#a7f3d0;margin:5px 0 0;font-size:13px'>স্বাস্থ্য বিভাগ — EPI টিকা কার্যক্রম</p>"
            + "</div>"
            + "<div style='padding:28px'>"
            + "<h3 style='color:#065f46;margin-top:0'>💉 টিকা সফলভাবে দেওয়া হয়েছে</h3>"
            + "<p style='color:#374151'>প্রিয় অভিভাবক,</p>"
            + "<p style='color:#374151'><strong>" + child.getChildName() + "</strong> এর আজকের টিকা সম্পন্ন হয়েছে।</p>"
            + "<div style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:10px;padding:16px 20px;margin:20px 0'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + row("শিশুর নাম",    child.getChildName())
            + row("কার্ড নম্বর",  child.getCardNo())
            + row("টিকার নাম",    given.getVaccineName() + " — Dose " + given.getDoseNo())
            + row("প্রদান তারিখ", given.getGivenDate().format(BD_DATE))
            + row("স্বাস্থ্যকেন্দ্র", given.getHealthCenter() != null ? given.getHealthCenter() : "-")
            + row("প্রদানকারী",   given.getGivenBy() != null ? given.getGivenBy() : "-")
            + "</table></div>"
            + "<div style='background:#fffbeb;border:1px solid #fde68a;border-radius:10px;padding:14px 18px;margin:16px 0'>"
            + "<p style='color:#92400e;font-size:13px;margin:0'>"
            + "<strong>⏭ পরবর্তী টিকা:</strong> " + nextInfo + "</p>"
            + "</div>"
            + "</div>"
            + footer()
            + "</div></body></html>";
    }

    private String buildMissedDoseEmail(EpiChild child, EpiVaccination missed) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:\"Segoe UI\",Arial,sans-serif;background:#f1f5f9;margin:0;padding:20px'>"
            + "<div style='max-width:620px;margin:0 auto;background:#fff;border-radius:14px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1)'>"
            + "<div style='background:linear-gradient(135deg,#7f1d1d,#991b1b);padding:28px;border-bottom:4px solid #f59e0b'>"
            + "<h2 style='color:#fff;margin:0;font-size:20px'>গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>"
            + "<p style='color:#fecaca;margin:5px 0 0;font-size:13px'>স্বাস্থ্য বিভাগ — EPI টিকা কার্যক্রম</p>"
            + "</div>"
            + "<div style='padding:28px'>"
            + "<h3 style='color:#991b1b;margin-top:0'>⚠️ টিকার ডোজ মিস হয়েছে</h3>"
            + "<p style='color:#374151'>প্রিয় অভিভাবক,</p>"
            + "<p style='color:#374151'><strong>" + child.getChildName() + "</strong> এর একটি গুরুত্বপূর্ণ টিকার ডোজ মিস হয়েছে।</p>"
            + "<div style='background:#fef2f2;border:1px solid #fecaca;border-radius:10px;padding:16px 20px;margin:20px 0'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + row("শিশুর নাম",       child.getChildName())
            + row("কার্ড নম্বর",     child.getCardNo())
            + row("মিস টিকা",        missed.getVaccineName() + " — Dose " + missed.getDoseNo())
            + row("নির্ধারিত তারিখ", missed.getScheduledDate().format(BD_DATE))
            + "</table></div>"
            + "<p style='color:#dc2626;font-weight:600;font-size:14px'>অনুগ্রহ করে যত দ্রুত সম্ভব নিকটস্থ স্বাস্থ্যকেন্দ্রে যোগাযোগ করুন।</p>"
            + "</div>"
            + footer()
            + "</div></body></html>";
    }


    private String buildApprovalEmail(EpiChild child) {
        // Build vaccine schedule table rows from EPI_SCHEDULE
        LocalDate dob = child.getDateOfBirth();
        StringBuilder rows = new StringBuilder();
        for (Object[] s : EPI_SCHEDULE) {
            String vax  = (String) s[0];
            String dose = (String) s[1];
            int    days = (int)    s[2];
            LocalDate dt = dob.plusDays(days);
            rows.append("<tr>")
                .append("<td style=\'padding:7px 10px;border-bottom:1px solid #e5e7eb\'>").append(vax).append(" — Dose ").append(dose).append("</td>")
                .append("<td style=\'padding:7px 10px;border-bottom:1px solid #e5e7eb;color:#059669;font-weight:600\'>").append(dt.format(BD_DATE)).append("</td>")
                .append("</tr>");
        }

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:\"Segoe UI\",Arial,sans-serif;background:#f1f5f9;margin:0;padding:20px'>"
            + "<div style='max-width:640px;margin:0 auto;background:#fff;border-radius:14px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.1)'>"
            + "<div style='background:linear-gradient(135deg,#064e3b,#065f46);padding:28px;border-bottom:4px solid #f59e0b'>"
            + "<h2 style='color:#fff;margin:0;font-size:20px'>গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>"
            + "<p style='color:#a7f3d0;margin:5px 0 0;font-size:13px'>স্বাস্থ্য বিভাগ — EPI টিকা কার্যক্রম</p>"
            + "</div>"
            + "<div style='padding:28px'>"
            + "<h3 style='color:#065f46;margin-top:0'>✅ EPI কার্ড অনুমোদিত হয়েছে</h3>"
            + "<p>প্রিয় অভিভাবক, <strong>" + child.getChildName() + "</strong> এর EPI নিবন্ধন অনুমোদিত হয়েছে।</p>"
            + "<div style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:10px;padding:16px 20px;margin:16px 0'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + row("EPI কার্ড নম্বর", child.getCardNo())
            + row("শিশুর নাম",       child.getChildName())
            + row("জন্ম তারিখ",      child.getDateOfBirth().format(BD_DATE))
            + row("ওয়ার্ড",           child.getWard() != null ? child.getWard() : "-")
            + "</table></div>"
            + "<h4 style='color:#064e3b;margin:20px 0 10px'>📅 সম্পূর্ণ টিকার সময়সূচি</h4>"
            + "<table style='width:100%;border-collapse:collapse;font-size:13px;border:1px solid #e5e7eb;border-radius:8px;overflow:hidden'>"
            + "<thead><tr style='background:#085041'>"
            + "<th style='padding:9px 10px;color:#a7f3d0;font-size:11px;text-align:left'>টিকার নাম</th>"
            + "<th style='padding:9px 10px;color:#a7f3d0;font-size:11px;text-align:left'>নির্ধারিত তারিখ</th>"
            + "</tr></thead><tbody>" + rows + "</tbody></table>"
            + "<p style='color:#6b7280;font-size:12px;margin-top:16px'>✅ টিকার কার্ড download করতে আমাদের portal-এ login করুন।</p>"
            + "</div>" + footer() + "</div></body></html>";
    }

    // Helper for table rows
    private String row(String label, String value) {
        return "<tr><td style='color:#6b7280;font-size:13px;padding:6px 0;width:45%'>" + label + "</td>"
             + "<td style='color:#111827;font-weight:600;font-size:13px;padding:6px 0'>"
             + (value != null ? value : "-") + "</td></tr>";
    }

    private String footer() {
        return "<div style='background:#f8fafc;padding:16px 28px;border-top:1px solid #e2e8f0;text-align:center'>"
             + "<p style='color:#94a3b8;font-size:11px;margin:0'>এটি একটি স্বয়ংক্রিয় বার্তা। এই email-এ reply করবেন না।<br>"
             + "EPI টিকা কার্যক্রম | স্বাস্থ্য বিভাগ | গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</p></div>";
    }
}
