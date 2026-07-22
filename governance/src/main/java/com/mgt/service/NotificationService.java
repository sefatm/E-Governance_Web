package com.mgt.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mgt.dao.NotificationMessageDAO;
import com.mgt.service.FcmService;
import com.mgt.dao.UserDAO;
import com.mgt.model.NotificationMessage;

@Service
public class NotificationService {

    @Autowired
    private NotificationMessageDAO notificationDAO;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private FcmService fcmService;

    @Autowired
    private UserDAO userDAO;


    // SEND NOTIFICATION
    public NotificationMessage send(NotificationMessage msg) {

        if (msg.getMessage() == null || msg.getMessage().isBlank()) {
            throw new RuntimeException("Message cannot be empty.");
        }

        if (msg.getType() == null || msg.getType().isBlank()) {
            throw new RuntimeException("Notification type is required.");
        }

        if (msg.getRecipientType() == null || msg.getRecipientType().isBlank()) {
            throw new RuntimeException("Recipient type is required.");
        }

        if ("Individual".equals(msg.getRecipientType())
                && (msg.getRecipientVal() == null
                || msg.getRecipientVal().isBlank())) {

            throw new RuntimeException(
                    "Recipient contact is required for individual notification."
            );
        }

        msg.setCreatedAt(LocalDateTime.now());

        switch (msg.getType()) {

            // SMS
            case "SMS" -> {

                if ("Individual".equals(msg.getRecipientType())) {

                    boolean ok = smsService.send(
                            msg.getRecipientVal(),
                            msg.getMessage()
                    );

                    msg.setStatus(ok ? "Sent" : "Failed");

                } else if ("All".equals(msg.getRecipientType())) {

                    sendBulkSmsAsync(msg.getMessage());
                    msg.setStatus("Queued");

                } else if ("Ward".equals(msg.getRecipientType())) {

                    msg.setStatus("Queued");
                }
            }

            // EMAIL
            case "Email" -> {

                String subject = msg.getTitle() != null
                        ? msg.getTitle()
                        : "Notification from E-Governance Portal";

                if ("Individual".equals(msg.getRecipientType())) {

                    emailService.sendHtml(
                            msg.getRecipientVal(),
                            subject,
                            buildEmailHtml(msg)
                    );

                    msg.setStatus("Sent");

                } else if ("All".equals(msg.getRecipientType())) {

                    sendBulkEmailAsync(
                            subject,
                            buildEmailHtml(msg)
                    );

                    msg.setStatus("Queued");

                } else if ("Ward".equals(msg.getRecipientType())) {

                    msg.setStatus("Queued");
                }
            }

            // PUSH (FCM)
            case "Push" -> {

                String pushTitle = msg.getTitle() != null
                        ? msg.getTitle()
                        : "E-Governance নোটিফিকেশন";

                if ("Individual".equals(msg.getRecipientType())) {
                    // Individual FCM token দিয়ে পাঠাও
                    boolean ok = fcmService.sendToToken(
                            msg.getRecipientVal(),
                            pushTitle,
                            msg.getMessage()
                    );
                    msg.setStatus(ok ? "Sent" : "Failed");

                } else {
                    // All Citizens — "all_citizens" topic
                    boolean ok = fcmService.sendToTopic(
                            "all_citizens",
                            pushTitle,
                            msg.getMessage()
                    );
                    msg.setStatus(ok ? "Sent" : "Failed");
                }
            }

            // UNKNOWN
            default -> throw new RuntimeException(
                    "Unknown notification type: " + msg.getType()
            );
        }

        return notificationDAO.save(msg);
    }

    @Async
    private void sendBulkEmailAsync(String subject, String emailHtml) {
        List<String> emails = userDAO.getAll().stream()
            .filter(u -> "Active".equalsIgnoreCase(u.getStatus()))
            .map(u -> u.getEmail())
            .filter(e -> e != null && !e.isBlank())
            .distinct()
            .collect(Collectors.toList());

        System.out.println("[NotificationService] Bulk Email to " + emails.size() + " active users");
        emails.forEach(email -> emailService.sendHtml(email, subject, emailHtml));
	}
    

 // Bulk SMS async (non-blocking) 
    @Async
    public void sendBulkSmsAsync(String message) {

        List<String> mobiles = userDAO.getAll().stream()
            .filter(u -> "Active".equalsIgnoreCase(u.getStatus()))
            .map(u -> "01700000000")
            .filter(c -> c != null && !c.isBlank())
            .collect(Collectors.toList());

        System.out.println("[NotificationService] Bulk SMS to " + mobiles.size() + " citizens");

        smsService.sendBulk(mobiles, message);
    }

    // EMAIL HTML
    private String buildEmailHtml(NotificationMessage msg) {

        String title = msg.getTitle() != null
                ? msg.getTitle()
                : "Notification";

        String content = msg.getMessage() != null
                ? msg.getMessage()
                : "";

        String tag = msg.getServiceTag() != null
                ? msg.getServiceTag()
                : "General";

        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "</head>"

                + "<body style='font-family:Arial,sans-serif;"
                + "background:#f4f4f4;margin:0;padding:0'>"

                + "<div style='max-width:600px;"
                + "margin:30px auto;background:#fff;"
                + "border-radius:10px;overflow:hidden;"
                + "box-shadow:0 2px 10px rgba(0,0,0,.1)'>"

                + "<div style='background:#064e3b;"
                + "padding:24px 28px;"
                + "border-bottom:4px solid #f59e0b'>"

                + "<h2 style='color:#fff;margin:0;'>"
                + "গণপ্রজাতন্ত্রী বাংলাদেশ সরকার"
                + "</h2>"

                + "<p style='color:#d1fae5;"
                + "margin-top:5px;font-size:12px'>"
                + "E-Governance Municipal Portal"
                + "</p>"

                + "</div>"

                + "<div style='padding:28px'>"

                + "<h3 style='color:#0f172a'>"
                + title
                + "</h3>"

                + "<p style='color:#374151;"
                + "line-height:1.7;white-space:pre-line'>"
                + content
                + "</p>"

                + "<div style='margin-top:20px;"
                + "padding:10px 14px;"
                + "background:#f0fdf4;"
                + "border-left:3px solid #059669;"
                + "border-radius:4px;"
                + "font-size:12px'>"

                + "Category: "
                + tag

                + "</div>"

                + "</div>"

                + "<div style='background:#f8fafc;"
                + "padding:16px 28px;"
                + "font-size:11px;color:#94a3b8;"
                + "border-top:1px solid #e2e8f0'>"

                + "This is an automated message from "
                + "E-Governance Municipal Portal."

                + "</div>"

                + "</div>"
                + "</body>"
                + "</html>";
    }

    // CRUD
    public List<NotificationMessage> getAll() {
        return notificationDAO.getAll();
    }

    public NotificationMessage getById(int id) {
        return notificationDAO.getById(id);
    }

    public List<NotificationMessage> getByType(String t) {
        return notificationDAO.getByType(t);
    }

    public List<NotificationMessage> getByTag(String tag) {
        return notificationDAO.getByServiceTag(tag);
    }

    public void delete(int id) {
        notificationDAO.delete(id);
    }

    // SUMMARY
    public Map<String, Object> getSummary() {

        Map<String, Object> map = new HashMap<>();

        map.put(
                "totalSms",
                notificationDAO.countByType("SMS")
        );

        map.put(
                "totalEmail",
                notificationDAO.countByType("Email")
        );

        map.put(
                "totalPush",
                notificationDAO.countByType("Push")
        );

        map.put(
                "total",
                notificationDAO.getAll().size()
        );

        return map;
    }
}
