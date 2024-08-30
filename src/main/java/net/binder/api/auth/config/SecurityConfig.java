package net.binder.api.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.binder.api.auth.filter.JwtFilter;
import net.binder.api.auth.handler.CustomAuthenticationEntryPoint;
import net.binder.api.auth.handler.CustomAuthenticationSuccessHandler;
import net.binder.api.auth.service.CustomOAuth2UserService;
import net.binder.api.auth.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;

    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final JwtUtil jwtUtil;

    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(getCorsConfigurationSource()));

        http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(customOAuth2UserService))
                        .successHandler(customAuthenticationSuccessHandler));

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/").permitAll() // AWS 헬스 체크
                        .requestMatchers("/swagger-ui/**", "/v3/**").permitAll() // 스웨거
                        .anyRequest().authenticated())
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)); // 미인증시

        http.
                addFilterBefore(new JwtFilter(jwtUtil, objectMapper), UsernamePasswordAuthenticationFilter.class);

        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    private CorsConfigurationSource getCorsConfigurationSource() {
        return request -> {
            String origin = request.getHeader("Origin");

            CorsConfiguration configuration = new CorsConfiguration();
            if (origin != null) {
                configuration.setAllowedOrigins(List.of(origin));
            }

            configuration.setAllowedMethods(List.of("*"));
            configuration.setAllowCredentials(true);
            configuration.setAllowedHeaders(List.of("*"));

            configuration.setExposedHeaders(List.of("Authorization"));

            return configuration;
        };
    }
}
