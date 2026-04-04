package com.karim.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.karim.util.CustomAccessDeniedHandler;
import com.karim.util.JwtFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtFilter filter;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // -------- Public & Swagger --------
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/swagger-resources/**", "/webjars/**",
                                "/*.html", "/*.css", "/*.js", "/images/**", "/favicon.ico"
                        ).permitAll()

                        // -------- FIX 1: Public tracking endpoint (no token needed) --------
                        // Customer clicks link from email — they have no JWT token.
                        // Only the track endpoint is public; all other /api/delivery/** still require DELIVERY role.
                        .requestMatchers(HttpMethod.GET, "/api/delivery/track/**").permitAll()

                        // -------- FIX 2: WebSocket handshake endpoint (public) --------
                        // SockJS connects here — must be permitted before the JWT filter blocks it.
                        .requestMatchers("/ws/**","/api/chat/message").permitAll()

                        // -------- CORS preflight --------
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        .requestMatchers("/api/chat/agent-help").hasRole("DELIVERY")

                        // -------- Products --------
                        .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")

                        // -------- Orders --------
                        .requestMatchers(HttpMethod.GET, "/api/order/all").hasRole("ADMIN")
                        .requestMatchers("/api/order/**").hasAnyRole("USER", "ADMIN")

                        // -------- Cart --------
                        .requestMatchers("/api/cart/**").hasRole("USER")

                        // -------- Payment --------
                        .requestMatchers("/api/payment/**").hasRole("USER")

                        // -------- Admin --------
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/stock/**").hasRole("ADMIN")

                        // -------- User management --------
                        .requestMatchers("/api/user/**").hasRole("ADMIN")

                        // -------- Delivery (agents only — track/** already permitted above) --------
                        .requestMatchers("/api/delivery/**").hasRole("DELIVERY")

                        // -------- Addresses --------
                        .requestMatchers("/api/addresses/**").hasAnyRole("USER", "ADMIN", "DELIVERY")

                        // -------- Any other --------
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .cors();

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfig() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:9090"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}