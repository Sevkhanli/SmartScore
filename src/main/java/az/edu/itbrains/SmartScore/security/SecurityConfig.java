package az.edu.itbrains.SmartScore.security;

import lombok.RequiredArgsConstructor;
import az.edu.itbrains.SmartScore.handlers.AuthDeniedHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final AuthDeniedHandler authDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configure(http))
                .authorizeHttpRequests(auth -> auth
                        // 1. Swagger & Public Docs
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // 2. Auth & Public Endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/v1/hero/active").permitAll()
                        .requestMatchers("/api/v1/features/public/**").permitAll()

                        // 3. Analiz API-ları (Bunu əlavə etdim ki, test edə biləsən)
                        .requestMatchers("/api/analysis/**").permitAll()

                        // 4. Admin Endpoints
                        .requestMatchers("/api/v1/hero/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/features/admin/**").hasRole("ADMIN")

                        // 5. Auth tələb edənlər
                        .requestMatchers("/api/auth/me", "/api/auth/logout").authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authDeniedHandler)
                        .accessDeniedHandler(authDeniedHandler)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // WebSecurityCustomizer-i sildim, çünki permitAll() artıq yuxarıda həmin işi görür və xəta vermir.

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}