package com.mgt.service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    @Value("${sms.ssl.api.url:https://sms.sslwireless.com/pushapi/dynamic/server.php}")
    private String apiUrl;

    @Value("${sms.ssl.api.token:NOT_SET}")
    private String apiToken;

    @Value("${sms.ssl.sender.id:E-GOV}")
    private String senderId;

    private final OkHttpClient httpClient = new OkHttpClient();

    public boolean send(String mobile, String message) {
        if ("NOT_SET".equals(apiToken)) {
            System.err.println("[SmsService] API token not configured — skipping SMS to " + mobile);
            return false;
        }

        String normalizedMobile = mobile.startsWith("0") ? "88" + mobile : mobile;

        try {
            String encodedMsg = URLEncoder.encode(message, StandardCharsets.UTF_8);
            String url = apiUrl
                + "?api_token=" + apiToken
                + "&sid="       + senderId
                + "&msisdn="    + normalizedMobile
                + "&sms="       + encodedMsg
                + "&csms_id="   + System.currentTimeMillis();

            Request request = new Request.Builder().url(url).get().build();

            try (Response response = httpClient.newCall(request).execute()) {
                String body = response.body() != null ? response.body().string() : "";
                if (response.isSuccessful()) {
                    System.out.println("[SmsService] SMS sent to " + mobile + " | Response: " + body);
                    return true;
                } else {
                    System.err.println("[SmsService] SMS failed to " + mobile + " | HTTP " + response.code() + " | " + body);
                    return false;
                }
            }
        } catch (IOException e) {
            System.err.println("[SmsService] Network error sending SMS to " + mobile + ": " + e.getMessage());
            return false;
        }
    }

    public void sendBulk(java.util.List<String> mobiles, String message) {
        mobiles.forEach(m -> send(m, message));
    }
}
