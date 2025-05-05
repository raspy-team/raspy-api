package com.raspy.backend.jwt

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            // 기본 FormLogin / HttpBasic 완전 비활성화
            .formLogin { it.disable() }
            .httpBasic { it.disable() }

            .authorizeHttpRequests {
                it
                    // 인증, 회원가입, H2 콘솔
                    .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()

                    // OpenAPI JSON/YAML
                    .requestMatchers(
                        "/v3/api-docs",            // 루트
                        "/v3/api-docs/**",         // 서브 경로
                        "/v3/api-docs.yaml"        // YAML 포맷
                    ).permitAll()

                    // Swagger UI / ReDoc UI
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/doc.html"
                    ).permitAll()

                    // Actuator (헬스 체크)
                    .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                    // Admin 전용
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")

                    // 나머지는 USER 또는 ADMIN 권한 필요
                    .anyRequest().hasAnyRole("USER", "ADMIN")
            }

            // H2 콘솔 iframe 허용
            .headers { it.frameOptions { it.sameOrigin() } }

            // JWT 필터 등록
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
