package org.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.repositories.RevokedTokenRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final RevokedTokenRepository revokedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 1. Header yoxlanışı
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        // 2. KRİTİK YOXLANIŞ: Token ləğv edilibsə, heç bir metoda (logout daxil) keçid vermə
        if (revokedTokenRepository.existsByToken(token)) {
            sendErrorResponse(response, "Bu token artıq etibarsızdır. Yenidən giriş edin.");
            return;
        }

        try {
            String email = jwtService.findUsername(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userDetailsService.loadUserByUsername(email);

                // Token tipini yoxlayırıq (Yalnız ACCESS tokenlərə icazə veririk)
                String tokenType = jwtService.exportToken(token, claims -> (String) claims.get("type"));

                if ("ACCESS".equals(tokenType) && jwtService.tokenControl(token, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Tokenin vaxtı bitibsə və ya strukturu pozulubsa bura düşür.
            // Əgər endpoint qorunursa, Spring Security özü 403/401 qaytaracaq.
        }

        filterChain.doFilter(request, response);
    }

    // Xəta mesajını səliqəli JSON formatında qaytarmaq üçün köməkçi metod
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\": false, \"message\": \"" + message + "\"}");
    }
}