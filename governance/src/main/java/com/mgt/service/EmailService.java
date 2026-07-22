package com.mgt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

/**
 * EmailService — সব email @Async তে পাঠানো হয়।
 * এতে email failure-এ main thread / HTTP response block হয় না।
 */
@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${mail.from:noreply@governance.gov.bd}")
    private String fromAddress;

    @Value("${mail.from.name:E-Governance Municipal Portal}")
    private String fromName;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    private boolean mailReady() {
        if (mailSender == null) {
            System.err.println("[EmailService] JavaMailSender not configured — skipping.");
            return false;
        }
        if (mailPassword == null || mailPassword.isBlank()) {
            System.err.println("[EmailService] MAIL_PASSWORD is empty — email sending is disabled.");
            return false;
        }
        return true;
    }

    @Async
    public void sendSimple(String to, String subject, String body) {
        if (!mailReady()) return;
        try {
            if (to == null || to.isBlank() || !to.contains("@")) {
                System.err.println("[EmailService] Invalid recipient — skipping: " + to);
                return;
            }
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromAddress);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            System.out.println("[EmailService] ✅ Simple email sent to: " + to);
        } catch (Exception e) {
            System.err.println("[EmailService] ❌ Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    @Async
    public void sendHtml(String to, String subject, String htmlBody) {
        if (!mailReady()) return;
        try {
            if (to == null || to.isBlank() || !to.contains("@")) {
                System.err.println("[EmailService] Invalid recipient — skipping: " + to);
                return;
            }
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(mime);
            System.out.println("[EmailService] ✅ HTML email sent to: " + to);
        } catch (Exception e) {
            System.err.println("[EmailService] ❌ Failed to send HTML email to " + to + ": " + e.getMessage());
        }
    }

    /**
     * HTML email + PDF attachment পাঠায়।
     * Payment receipt email-এ ব্যবহার হয়।
     */
    @Async
    public void sendHtmlWithAttachment(String to, String subject,
                                       String htmlBody,
                                       String attachmentName, byte[] attachmentBytes) {
        if (!mailReady()) return;
        try {
            if (to == null || to.isBlank() || !to.contains("@")) {
                System.err.println("[EmailService] Invalid recipient — skipping: " + to);
                return;
            }
            MimeMessage mime = mailSender.createMimeMessage();
            // multipart=true for attachment
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(fromAddress, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            // PDF attachment
            helper.addAttachment(attachmentName, new ByteArrayResource(attachmentBytes));
            mailSender.send(mime);
            System.out.println("[EmailService] ✅ Email with attachment sent to: " + to);
        } catch (Exception e) {
            System.err.println("[EmailService] ❌ Failed to send email with attachment to "
                               + to + ": " + e.getMessage());
        }
    }
}
