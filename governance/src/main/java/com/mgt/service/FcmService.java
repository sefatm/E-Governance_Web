package com.mgt.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * ══════════════════════════════════════════════════════════════════
 *  FCM Push Notification Service — Firebase Admin SDK
 * ══════════════════════════════════════════════════════════════════
 *
 *  SETUP (একবার করতে হবে):
 *
 *  1. Firebase Console → Project Settings → Service Accounts
 *     → Generate New Private Key → JSON file download করো
 *
 *  2. JSON file টি rename করো: firebase-service-account.json
 *     এবং রাখো: src/main/resources/firebase-service-account.json
 *
 *  3. pom.xml এ add করো:
 *     <dependency>
 *       <groupId>com.google.firebase</groupId>
 *       <artifactId>firebase-admin</artifactId>
 *       <version>9.2.0</version>
 *     </dependency>
 *
 *  4. application.properties এ add করো:
 *     fcm.enabled=true
 *
 *  5. Angular-এ FCM web push setup:
 *     ng add @angular/fire → firebaseConfig দিয়ে configure করো
 *     getToken() দিয়ে device FCM token পাও → backend-এ save করো
 *
 *  TOPIC APPROACH (Recommended):
 *    - সব citizen app-এ "all_citizens" topic-এ subscribe করে
 *    - Admin একটা sendToTopic() call = সবাই পায়
 *    - Individual এর জন্য user-specific token DB-তে save করতে হবে
 * ══════════════════════════════════════════════════════════════════
 */
@Service
public class FcmService {

    @Value("${fcm.enabled:false}")
    private boolean fcmEnabled;

    @PostConstruct
    public void initialize() {
        if (!fcmEnabled) {
            System.out.println("[FcmService] FCM disabled — set fcm.enabled=true in application.properties");
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) return;

        try {
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
            if (!resource.exists()) {
                System.err.println("[FcmService] firebase-service-account.json not found in resources/");
                return;
            }

            InputStream serviceAccount = resource.getInputStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("[FcmService] Firebase initialized successfully ✅");

        } catch (IOException e) {
            System.err.println("[FcmService] Firebase initialization failed: " + e.getMessage());
        }
    }

    // ── Send to topic (All citizens broadcast) ────────────────────────────────
    public boolean sendToTopic(String topic, String title, String body) {
        if (!fcmEnabled || FirebaseApp.getApps().isEmpty()) {
            System.out.println("[FcmService] Push skipped (not configured) — topic: " + topic
                    + " | title: " + title);
            return false;
        }

        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("click_action", "/dashboard")
                    .putData("topic", topic)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("[FcmService] Push sent to topic '" + topic + "': " + response);
            return true;

        } catch (Exception e) {
            System.err.println("[FcmService] Failed to send push to topic " + topic + ": " + e.getMessage());
            return false;
        }
    }

    // ── Send to individual FCM token ──────────────────────────────────────────
    public boolean sendToUser(String fcmToken, String title, String body) {
        if (!fcmEnabled || FirebaseApp.getApps().isEmpty()) {
            System.out.println("[FcmService] Push skipped (not configured) — token: " + fcmToken);
            return false;
        }

        if (fcmToken == null || fcmToken.isBlank()) {
            System.err.println("[FcmService] FCM token is empty for user");
            return false;
        }

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("click_action", "/dashboard")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("[FcmService] Push sent to user: " + response);
            return true;

        } catch (Exception e) {
            System.err.println("[FcmService] Failed to send push to user: " + e.getMessage());
            return false;
        }
    }

    // ── Subscribe tokens to topic ─────────────────────────────────────────────
    /** Alias for sendToUser — called from NotificationService */
    public boolean sendToToken(String fcmToken, String title, String body) {
        return sendToUser(fcmToken, title, body);
    }

    public void subscribeToTopic(List<String> tokens, String topic) {
        if (!fcmEnabled || FirebaseApp.getApps().isEmpty()) return;
        if (tokens == null || tokens.isEmpty()) return;

        try {
            TopicManagementResponse response =
                FirebaseMessaging.getInstance().subscribeToTopic(tokens, topic);
            System.out.println("[FcmService] Subscribed " + response.getSuccessCount()
                    + " tokens to topic: " + topic);
        } catch (Exception e) {
            System.err.println("[FcmService] Subscribe failed: " + e.getMessage());
        }
    }
}
