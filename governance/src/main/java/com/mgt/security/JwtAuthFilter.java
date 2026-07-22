package com.mgt.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper mapper = new ObjectMapper();

    // Public endpoints — filter skip করবে
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/refresh",
        "/api/auth/forgot-password",
        "/api/auth/reset-password",
        "/api/notice/active",
        "/uploads/",
        "/api/voter/verify",
        "/api/otp/",
        "/api/farmer/g2p/callback",
        "/api/family-card/apply",
        "/api/family-card/check/",
        "/api/family-card/download/",
        "/api/farmer-card/apply",
        "/api/farmer-card/check/",
        "/api/farmer-card/download/",
        "/api/lpg-card/apply",
        "/api/lpg-card/check/",
        "/api/lpg-card/download/",
        "/api/vgd-card/apply",
        "/api/vgd-card/check/",
        "/api/vgd-card/download/",
        "/api/citizen/by-contact/",
        "/api/citizen/verify/",
        "/api/family/generate-pdf/",
        "/api/tradeLicense/certificate/",
        "/api/trade-renewal/certificate/",
        "/api/birth-death/download/",
        "/api/citizen/download/",
        "/api/epi/generate-pdf/",
        "/api/holding-new-registration/generate-pdf/",
        "/api/ownership-transfer/generate-pdf/"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // /api/auth/profile/{id} — GET only public
        if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/auth/profile/")) return true;

        // Citizen-facing holding registration submission must work before/without JWT.
        // Staff-only listing and approval endpoints are still protected in SecurityConfig.
        if ("POST".equalsIgnoreCase(method)
                && (path.equals("/api/holding-new-registration/create")
                    || path.startsWith("/api/holding-new-registration/upload/"))) {
            return true;
        }

        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        String reqPath = request.getRequestURI();
        boolean isPdfEndpoint = reqPath.contains("/generate-pdf/");

        if (header == null || !header.startsWith("Bearer ")) {
            if (isPdfEndpoint) {
                chain.doFilter(request, response);
                return;
            }
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                "MISSING_TOKEN", "Authorization token is required.");
            return;
        }

        String token = header.substring(7).trim();

        if (!jwtUtil.isAccessTokenValid(token)) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                "TOKEN_EXPIRED", "Access token expired or invalid. Please refresh.");
            return;
        }

        try {
            Integer userId = jwtUtil.getUserId(token);
            String role    = jwtUtil.getRole(token);
            String email   = jwtUtil.getEmail(token);

            String springRole = "ROLE_" + normalizeRole(role);

            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    userId, null,
                    List.of(new SimpleGrantedAuthority(springRole))
                );

            SecurityContextHolder.getContext().setAuthentication(auth);

            request.setAttribute("userId", userId);
            request.setAttribute("role", role);
            request.setAttribute("email", email);

        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                "TOKEN_PARSE_ERROR", "Could not process token.");
            return;
        }

        chain.doFilter(request, response);
    }

    // ─── Role string normalize: "Admin / Municipal Officer" → "Admin_Municipal_Officer"
    private String normalizeRole(String role) {
        if (role == null) return "UNKNOWN";
        return role.trim()
                   .replaceAll("[^a-zA-Z0-9]", "_")
                   .replaceAll("_+", "_")
                   .replaceAll("^_|_$", "");
    }

    private void sendError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
            mapper.writeValueAsString(Map.of("code", code, "message", message))
        );
    }
}
