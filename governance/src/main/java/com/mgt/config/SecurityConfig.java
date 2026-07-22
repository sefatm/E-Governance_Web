package com.mgt.config;

import com.mgt.security.JwtAuthFilter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * ════════════════════════════════════════════════════════════════════
 *  ROLE MAPPING (DB role string → Spring Security ROLE_ prefix)
 *
 *  DB role string                   Spring authority
 *  ────────────────────────────────────────────────────────────────
 *  "Super Admin"                 →  ROLE_Super_Admin
 *  "Admin / Municipal Officer"   →  ROLE_Admin___Municipal_Officer
 *  "Department Officer"          →  ROLE_Department_Officer
 *  "Project Officer"             →  ROLE_Project_Officer
 *  "Health / Sanitation Officer" →  ROLE_Health___Sanitation_Officer
 *  "Auditor / Accountant"        →  ROLE_Auditor___Accountant
 *  "ElectionOfficer"             →  ROLE_ElectionOfficer
 *  "Citizen"                     →  ROLE_Citizen
 *
 *  JwtAuthFilter.normalizeRole() করে এই mapping।
 *
 *  NOTE: hasAnyAuthority() ব্যবহার করা হয়েছে (hasAnyRole() নয়)।
 *        কারণ authority string-এ ROLE_ prefix explicitly আছে।
 *        hasAnyRole() internally ROLE_ যোগ করে — double prefix হয়ে যায়।
 * ════════════════════════════════════════════════════════════════════
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // @PreAuthorize controller-এ ব্যবহারের জন্য
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origin-patterns:http://localhost:4200,http://127.0.0.1:4200}")
    private List<String> allowedOriginPatterns;

    // ── Shorthand role constants ─────────────────────────────────────────
    // normalizeRole() output: non-alphanumeric→"_", consecutive "_"→single "_"
    // "Super Admin"                 → Super_Admin
    // "Admin / Municipal Officer"   → Admin_Municipal_Officer
    // "Health / Sanitation Officer" → Health_Sanitation_Officer
    // "Auditor / Accountant"        → Auditor_Accountant
    private static final String SUPER        = "ROLE_Super_Admin";
    private static final String ADMIN        = "ROLE_Admin_Municipal_Officer";
    private static final String ADMIN_SIMPLE = "ROLE_Admin";
    private static final String DEPT         = "ROLE_Department_Officer";
    private static final String PROJECT      = "ROLE_Project_Officer";
    private static final String HEALTH       = "ROLE_Health_Sanitation_Officer";
    private static final String ACCOUNTANT   = "ROLE_Auditor_Accountant";
    private static final String ELECTION     = "ROLE_ElectionOfficer";
    private static final String CITIZEN      = "ROLE_Citizen";

    // ── Role groups ──────────────────────────────────────────────────────
    /** Super Admin + Admin */
    private static final String[] ADMIN_UP   = { SUPER, ADMIN, ADMIN_SIMPLE };
    /** Any staff role */
    private static final String[] ALL_STAFF  = { SUPER, ADMIN, ADMIN_SIMPLE, DEPT, PROJECT, HEALTH, ACCOUNTANT, ELECTION };
    /** Finance access */
    private static final String[] FINANCE    = { SUPER, ADMIN, ADMIN_SIMPLE, DEPT, ACCOUNTANT };
    /** Infrastructure/project management */
    private static final String[] INFRA      = { SUPER, ADMIN, ADMIN_SIMPLE, DEPT, PROJECT };
    /** Health & sanitation management */
    private static final String[] HEALTH_MGMT= { SUPER, ADMIN, ADMIN_SIMPLE, DEPT, HEALTH };
    /** Social card admin */
    private static final String[] SOCIAL     = { SUPER, ADMIN, ADMIN_SIMPLE, DEPT };
    /** Election management */
    private static final String[] ELECTION_MGMT = { SUPER, ADMIN, ADMIN_SIMPLE, DEPT, ELECTION };
    /** Everyone including citizens */
    private static final String[] EVERYONE   = { SUPER, ADMIN, ADMIN_SIMPLE, DEPT, PROJECT, HEALTH, ACCOUNTANT, ELECTION, CITIZEN };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── 1. Preflight ────────────────────────────────────────
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // ── 2. Public — no token needed ─────────────────────────
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh",
                    "/api/auth/forgot-password",
                    "/api/auth/forgot-password/**",
                    "/api/auth/reset-password",
                    "/api/auth/reset-password/**",
                    "/api/notice/active",
                    "/uploads/**",
                    "/api/farmer/g2p/callback",
                    "/error"
                ).permitAll()
                .requestMatchers(HttpMethod.POST, "/api/voter/verify").permitAll()
                .requestMatchers("/api/otp/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/auth/profile/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/profile/**").authenticated()
                .requestMatchers(HttpMethod.PUT,  "/api/auth/profile/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/auth/upload-photo").authenticated()
                .requestMatchers(HttpMethod.PUT,  "/api/auth/change-password/**").authenticated()
                // citizen data — specific rules BEFORE broad rule (first-match wins)
                .requestMatchers(HttpMethod.GET,  "/api/citizen/verify/**").permitAll()
                // by-contact: citizen নিজের status দেখবে OTP দিয়ে — public (no token needed)
                .requestMatchers(HttpMethod.GET,  "/api/citizen/by-contact/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/citizen/download/**").permitAll()
                // citizen/getall: staff only
                .requestMatchers(HttpMethod.GET,  "/api/citizen/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION)
                .requestMatchers(HttpMethod.PUT,  "/api/citizen/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET,  "/api/citizen/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/citizen/**").authenticated()
                .requestMatchers(HttpMethod.GET,  "/api/birth-death/mobile/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/passport/mobile/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/family/mobile/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                // certificate two-step approval — staff only
                .requestMatchers(HttpMethod.PUT,  "/api/birth-death/approve/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/family/approve/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/citizen/approve/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/birth-death/seal/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/family/seal/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/citizen/seal/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                // birth-death, passport, family getall — all staff (not citizen)
                .requestMatchers(HttpMethod.GET,  "/api/birth-death/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION)
                .requestMatchers(HttpMethod.GET,  "/api/passport/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION)
                .requestMatchers(HttpMethod.GET,  "/api/family/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION)
                .requestMatchers(HttpMethod.POST, "/api/family-card/apply").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/family-card/check/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/tradeLicense/certificate/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/trade-renewal/certificate/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/family/generate-pdf/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/birth-death/download/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/epi/generate-pdf/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/holding-new-registration/generate-pdf/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/ownership-transfer/generate-pdf/**").permitAll()

                // ── 3. USER/ROLES management — Admin only ────────────────
                .requestMatchers("/api/users/**").hasAnyAuthority(SUPER, ADMIN, ADMIN_SIMPLE)

                // ── 4. System Settings — Admin only ──────────────────────
                .requestMatchers("/api/settings/**").hasAnyAuthority(SUPER, ADMIN, ADMIN_SIMPLE)

                // ── 5. Audit Logs — Super Admin only ─────────────────────
                .requestMatchers("/api/auditlog/**").hasAuthority(SUPER)
                .requestMatchers("/api/audit/**").hasAnyAuthority(SUPER, ADMIN, ELECTION)

                // ── 6. Notice management — Admin only (public read above) ─
                .requestMatchers(HttpMethod.POST,   "/api/notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.PUT,    "/api/notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.DELETE, "/api/notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.GET,    "/api/notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)

                // ── 7. Tax Assessment — Finance roles ────────────────────
                .requestMatchers(HttpMethod.GET, "/api/tax-assessment/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,ACCOUNTANT)
                .requestMatchers("/api/tax-assessment/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ACCOUNTANT)

                // ── 8. Tax Payment — all staff + citizen ─────────────────
                .requestMatchers(HttpMethod.POST, "/api/tax-payment/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/tax-payment/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/tax-due/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ACCOUNTANT)
                .requestMatchers("/api/tax-collection-report/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ACCOUNTANT)

                // ── 9. Trade License — mixed ──────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/tradeLicense/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/tradeLicense/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET,  "/api/tradeLicense/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT,  "/api/tradeLicense/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.DELETE,"/api/tradeLicense/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.POST, "/api/trade-renewal/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/trade-renewal/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/trade-renewal/approve/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/trade-renewal/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)

                // ── 9A. Holding registration — Citizen submit, staff process ─
                .requestMatchers(HttpMethod.POST, "/api/holding-new-registration/create").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/holding-new-registration/upload/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/holding-new-registration/getall").hasAnyAuthority(INFRA)
                .requestMatchers(HttpMethod.GET,  "/api/holding-new-registration/*").hasAnyAuthority(EVERYONE)
                .requestMatchers(HttpMethod.PUT,  "/api/holding-new-registration/approve/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/holding-new-registration/status/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/holding-new-registration/location/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)

                // ── 9B. Ownership transfer approval/pdf ───────────────────
                .requestMatchers(HttpMethod.POST, "/api/ownership-transfer/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,ACCOUNTANT,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/ownership-transfer/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,ACCOUNTANT)
                .requestMatchers(HttpMethod.PUT,  "/api/ownership-transfer/approve/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/ownership-transfer/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)

                // ── 10. Infrastructure — mixed ────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/road/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/road/my-applications").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/road/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers(HttpMethod.PUT,  "/api/road/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers(HttpMethod.POST, "/api/drainage/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/drainage/my-applications").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/drainage/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers(HttpMethod.PUT,  "/api/drainage/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers(HttpMethod.POST, "/api/street-light/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/street-light/my-applications").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/street-light/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers(HttpMethod.PUT,  "/api/street-light/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers(HttpMethod.POST, "/api/construction/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/construction/my-applications").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/construction/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers(HttpMethod.PUT,  "/api/construction/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)

                // ── 11. Health — mixed ────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/health-notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST,"/api/health-notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,HEALTH)
                .requestMatchers(HttpMethod.GET, "/api/health-center/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST,"/api/health-center/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,HEALTH)
                .requestMatchers(HttpMethod.PUT, "/api/health-center/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH)
                .requestMatchers("/api/sanitation/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,HEALTH)
                .requestMatchers(HttpMethod.POST,"/api/epi/register").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST,"/api/epi/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,HEALTH)
                .requestMatchers(HttpMethod.GET, "/api/epi/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT, "/api/epi/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,HEALTH)

                // ── 12. Complaint ─────────────────────────────────────────
                // singular: /api/complaint/** (form submissions)
                .requestMatchers(HttpMethod.POST, "/api/complaint/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/complaint/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT,  "/api/complaint/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                // plural: /api/complaints/** (admin list + citizen mobile tracking)
                .requestMatchers(HttpMethod.GET,  "/api/complaints/mobile/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/complaints/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.POST, "/api/complaints/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/complaints/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/complaints/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)

                // ── 13. Ward — Admin only ─────────────────────────────────
                .requestMatchers("/api/ward/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET, "/api/zone/**", "/api/center/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST, "/api/zone/**", "/api/center/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)

                // ── 14. Project ───────────────────────────────────────────
                .requestMatchers(HttpMethod.GET,  "/api/project-list/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST, "/api/project-list/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers(HttpMethod.PUT,  "/api/project-list/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers("/api/project-budget/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ACCOUNTANT)

                // ── 15. Water ─────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/water-connection/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/water-connection/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT,  "/api/water-connection/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.GET,  "/api/water-bill/lookup").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST, "/api/water-bill/pay/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/water-bill/receipt/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/water-bill/getall", "/api/water-bill/*").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,ACCOUNTANT)
                .requestMatchers(HttpMethod.POST, "/api/water-bill/create").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,ACCOUNTANT)
                .requestMatchers(HttpMethod.PUT,  "/api/water-bill/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,ACCOUNTANT)
                .requestMatchers(HttpMethod.DELETE, "/api/water-bill/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)

                // ── 16. Waste Management ──────────────────────────────────
                .requestMatchers(HttpMethod.GET,  "/api/garbage-schedule/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST, "/api/garbage-schedule/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/garbage-schedule/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.POST, "/api/waste-request/create").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/waste-request/phone/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/waste-request/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/waste-request/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.DELETE, "/api/waste-request/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.DELETE, "/api/garbage-schedule/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET,  "/api/smart-bin/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.POST, "/api/smart-bin/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/smart-bin/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.DELETE, "/api/smart-bin/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.GET,  "/api/waste-collection-log/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.POST, "/api/waste-collection-log/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)

                // ── 17. Payment ───────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/payment/initiate").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT,  "/api/payment/confirm/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT,  "/api/payment/fail/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT,  "/api/payment/refund/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ACCOUNTANT)
                .requestMatchers(HttpMethod.GET,  "/api/payment/transactions").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ACCOUNTANT)
                .requestMatchers(HttpMethod.GET,  "/api/payment/transactions/citizen/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/payment/transactions/status/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ACCOUNTANT)
                .requestMatchers(HttpMethod.GET,  "/api/payment/transactions/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/payment/receipts/verify/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/payment/receipts/pdf/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/payment/receipts/citizen/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/payment/receipts/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ACCOUNTANT)
                .requestMatchers(HttpMethod.GET,  "/api/payment/summary").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ACCOUNTANT)

                // ── 18. Communication ─────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/notification/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.GET,  "/api/notification/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST, "/api/feedback/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET,  "/api/feedback/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)

                // ── 19. Social Cards ──────────────────────────────────────
                .requestMatchers(HttpMethod.GET,  "/api/family-card/download/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/family-card/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET,  "/api/family-card/status/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT,  "/api/family-card/status/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.DELETE,"/api/family-card/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)

                .requestMatchers(HttpMethod.GET,  "/api/farmer-card/check/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/farmer-card/download/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/farmer-card/apply").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/farmer-card/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET,  "/api/farmer-card/lookup-by-cardno/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/farmer-card/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.DELETE,"/api/farmer-card/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)

                .requestMatchers(HttpMethod.GET,  "/api/lpg-card/check/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/lpg-card/download/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/lpg-card/apply").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/lpg-card/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET,  "/api/lpg-card/by-cardno/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET,  "/api/lpg-card/stock").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.POST, "/api/lpg-card/stock").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.POST, "/api/lpg-card/distribute").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET,  "/api/lpg-card/history/**", "/api/lpg-card/cycle-summary/**", "/api/lpg-card/dealer-history").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/lpg-card/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.DELETE,"/api/lpg-card/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)

                // /api/vgd/card/** — controller path
                .requestMatchers(HttpMethod.GET,  "/api/vgd/card/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/vgd/card/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.DELETE,"/api/vgd/card/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                // /api/vgd-card/** — dashboard service uses this path
                .requestMatchers(HttpMethod.GET,  "/api/vgd-card/check/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/vgd-card/download/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/vgd-card/apply").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/vgd-card/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.PUT,  "/api/vgd-card/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.DELETE,"/api/vgd-card/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)

                // TCB / Farmer / LPG / VGD distribution — Social admin only
                .requestMatchers("/api/tcb/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.POST,"/api/farmer/distribute").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET, "/api/farmer/stock").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.POST,"/api/farmer/stock").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET, "/api/farmer/subsidy-history/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET, "/api/farmer/cycle-summary/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)
                .requestMatchers(HttpMethod.GET, "/api/farmer/g2p/batches").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.POST,"/api/farmer/g2p/batch").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.PUT, "/api/farmer/g2p/batch/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.GET, "/api/farmer/g2p/batch/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers("/api/vgd/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT)

                // ── 20. E-Tender ──────────────────────────────────────────
                .requestMatchers(HttpMethod.GET, "/api/etender/notice/open").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/etender/notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST,"/api/etender/notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.PUT, "/api/etender/notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.DELETE,"/api/etender/notice/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.POST,"/api/etender/bid/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET, "/api/etender/bid/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers(HttpMethod.GET, "/api/etender/bid/mine").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT, "/api/etender/bid/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers("/api/etender/award/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)
                .requestMatchers("/api/etender/blacklist/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE)

                // ── 21. E-Voting ──────────────────────────────────────────
                .requestMatchers(HttpMethod.POST,"/api/election/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)
                .requestMatchers(HttpMethod.PUT, "/api/election/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)
                .requestMatchers(HttpMethod.DELETE,"/api/election/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)
                .requestMatchers(HttpMethod.GET, "/api/election/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.POST,"/api/voter/create").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET, "/api/voter/getall").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)
                .requestMatchers(HttpMethod.PUT, "/api/voter/approve/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)
                .requestMatchers(HttpMethod.PUT, "/api/voter/reject/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)
                .requestMatchers(HttpMethod.DELETE,"/api/voter/delete/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)
                .requestMatchers(HttpMethod.POST,"/api/nominee/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.GET, "/api/nominee/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT, "/api/nominee/approve/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)
                .requestMatchers(HttpMethod.PUT, "/api/nominee/reject/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)
                .requestMatchers("/api/vote/cast").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers("/api/vote/result/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers("/api/vote/analytics/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,ELECTION)

                // ── 22. Report & Analytics ────────────────────────────────
                // সব GET report endpoint — authenticated সব role দেখতে পারবে
                .requestMatchers(HttpMethod.GET, "/api/report/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers("/api/gis/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)

                // ── Map API ───────────────────────────────────────────────
                .requestMatchers(HttpMethod.GET,  "/api/map/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT,HEALTH,ACCOUNTANT,ELECTION,CITIZEN)
                .requestMatchers(HttpMethod.PUT,  "/api/map/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)
                .requestMatchers(HttpMethod.POST, "/api/map/**").hasAnyAuthority(SUPER,ADMIN,ADMIN_SIMPLE,DEPT,PROJECT)

                // ── 23. Catch-all — authenticated ─────────────────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(allowedOriginPatterns);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        config.setExposedHeaders(List.of("Authorization", "X-Access-Token", "X-Refresh-Token"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
