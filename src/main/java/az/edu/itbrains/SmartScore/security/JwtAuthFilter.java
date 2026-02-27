package az.edu.itbrains.SmartScore.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import az.edu.itbrains.SmartScore.repositories.RevokedTokenRepository;
import org.springframework.lang.NonNull;
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
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // 1. SWAGGER ÜÇÜN İSTİSNA: Bu yollar filter yoxlamasından keçmir
        if (path.contains("/v3/api-docs") ||
                path.contains("/swagger-ui") ||
                path.contains("/swagger-resources") ||
                path.contains("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        // 2. Header yoxlanışı
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7).trim();

        // 3. KRİTİK YOXLANIŞ: Token ləğv edilibsə (Logout olubsa)
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
            // Tokenin vaxtı bitibsə və ya xəta varsa, Spring Security özü 401/403 qaytaracaq
        }

        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\": false, \"message\": \"" + message + "\"}");
    }
}