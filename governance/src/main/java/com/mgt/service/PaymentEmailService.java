package com.mgt.service;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mgt.model.PaymentReceipt;
import com.mgt.model.PaymentTransaction;

/**
 * Payment সম্পন্ন হলে নাগরিকের email-এ রশিদ পাঠায়।
 * PDF attachment পাঠাতে PaymentReceiptPdfService ব্যবহার করে।
 */
@Service
public class PaymentEmailService {

    @Autowired
    private EmailService emailService;

    @Autowired
    private PaymentReceiptPdfService pdfService;

    /**
     * Payment confirm হওয়ার পর email পাঠায়।
     * PDF receipt attachment হিসেবে যায়।
     * @Async — background thread-এ চলে, HTTP response block হয় না।
     */
    @Async
    public void sendReceiptEmail(PaymentReceipt receipt, PaymentTransaction txn) {

        String toEmail = txn != null ? txn.getEmail() : null;
        if (toEmail == null || toEmail.isBlank()) {
            System.out.println("[PaymentEmailService] No email address — skipping receipt email.");
            return;
        }

        String subject = "পেমেন্ট রশিদ — " + receipt.getReceiptNo() +
                         " | " + serviceLabel(receipt.getServiceType());

        String html = buildHtml(receipt, txn);
        byte[] pdfBytes = pdfService.generate(receipt, txn);

        String fileName = "receipt-" + receipt.getReceiptNo() + ".pdf";

        // EmailService এর sendHtmlWithAttachment পাঠাচ্ছি
        emailService.sendHtmlWithAttachment(toEmail, subject, html, fileName, pdfBytes);
    }

    private String buildHtml(PaymentReceipt receipt, PaymentTransaction txn) {
        String date = receipt.getIssuedAt() != null
            ? receipt.getIssuedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
            : "—";

        String mobile = txn != null && txn.getMobile() != null ? txn.getMobile() : "—";

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <style>
                body { margin:0; padding:0; background:#f1f5f9; font-family:'Segoe UI',Arial,sans-serif; color:#1e293b; }
                .wrap { max-width:600px; margin:32px auto; background:#fff; border-radius:12px; overflow:hidden;
                         box-shadow:0 4px 24px rgba(0,0,0,0.08); }
                .header { background:linear-gradient(135deg,#064e3b,#059669); padding:32px 28px; text-align:center;
                           border-bottom:5px solid #f59e0b; }
                .header h1 { margin:0 0 4px; font-size:22px; color:#fff; }
                .header p  { margin:0; font-size:12px; color:rgba(255,255,255,0.75); }
                .success-badge { background:#f0fdf4; border:1px solid #86efac; border-radius:8px;
                                  margin:24px 28px 0; padding:16px; text-align:center; }
                .success-badge h2 { margin:0; font-size:18px; color:#16a34a; }
                .receipt-no { font-size:13px; color:#6b7280; margin-top:6px; }
                .receipt-no strong { color:#0369a1; font-family:monospace; font-size:14px; }
                .section { padding:20px 28px 0; }
                table { width:100%; border-collapse:collapse; font-size:14px; }
                td { padding:11px 14px; border-bottom:1px solid #f1f5f9; }
                td:first-child { color:#64748b; font-weight:600; background:#f8fafc; width:40%; }
                .amount-box { margin:24px 28px; background:#f0fdf4; border:2px solid #86efac;
                               border-radius:10px; padding:18px 22px; display:flex;
                               justify-content:space-between; align-items:center; }
                .amount-label { font-size:14px; font-weight:600; color:#065f46; }
                .amount-value { font-size:24px; font-weight:700; color:#16a34a; }
                .footer { background:#f8fafc; padding:20px 28px; text-align:center;
                           font-size:12px; color:#94a3b8; border-top:1px solid #e2e8f0; }
                .footer a { color:#059669; text-decoration:none; }
              </style>
            </head>
            <body>
            <div class="wrap">

              <div class="header">
                <p style="color:rgba(255,255,255,0.8);font-size:12px;margin-bottom:6px">
                  গণপ্রজাতন্ত্রী বাংলাদেশ সরকার<br>
                  GOVERNMENT OF THE PEOPLE'S REPUBLIC OF BANGLADESH
                </p>
                <h1>পেমেন্ট রশিদ</h1>
                <p>Official Payment Receipt — E-Governance Municipal Portal</p>
              </div>

              <div class="success-badge">
                <h2>✓ পেমেন্ট সফল হয়েছে!</h2>
                <p class="receipt-no">রশিদ নম্বর: <strong>%s</strong></p>
              </div>

              <div class="section">
                <table>
                  <tr><td>নাগরিকের নাম</td><td>%s</td></tr>
                  <tr><td>NID নম্বর</td><td>%s</td></tr>
                  <tr><td>মোবাইল</td><td>%s</td></tr>
                  <tr><td>সেবার ধরন</td><td>%s</td></tr>
                  <tr><td>বিবরণ</td><td>%s</td></tr>
                  <tr><td>পেমেন্ট পদ্ধতি</td><td>%s</td></tr>
                  <tr><td>Transaction Ref</td><td style="font-family:monospace">%s</td></tr>
                  <tr><td>পেমেন্টের সময়</td><td>%s</td></tr>
                </table>
              </div>

              <div class="amount-box">
                <span class="amount-label">মোট পরিশোধিত পরিমাণ</span>
                <span class="amount-value">৳ %,.2f</span>
              </div>

              <div class="footer">
                <p>এই ইমেইলে PDF রশিদ সংযুক্ত আছে। ভবিষ্যতে ব্যবহারের জন্য সংরক্ষণ করুন।</p>
                <p style="margin-top:8px">
                  © %d E-Governance Management System — Municipality
                </p>
              </div>

            </div>
            </body>
            </html>
            """.formatted(
                receipt.getReceiptNo(),
                receipt.getCitizenName(),
                receipt.getCitizenNid(),
                mobile,
                serviceLabel(receipt.getServiceType()),
                receipt.getDescription() != null ? receipt.getDescription() : "—",
                receipt.getMethod(),
                txn != null ? txn.getTxnRef() : "—",
                date,
                receipt.getAmount(),
                java.time.Year.now().getValue()
            );
    }

    private String serviceLabel(String type) {
        if (type == null) return "—";
        return switch (type) {
            case "WaterBill"        -> "পানি বিল — Water Bill";
            case "TradeLicense"     -> "ট্রেড লাইসেন্স — Trade License";
            case "HoldingTax"       -> "হোল্ডিং ট্যাক্স — Holding Tax";
            case "ETender"          -> "ই-টেন্ডার — E-Tender";
            case "BirthCertificate" -> "জন্ম সনদ — Birth Certificate";
            default -> type;
        };
    }
}
