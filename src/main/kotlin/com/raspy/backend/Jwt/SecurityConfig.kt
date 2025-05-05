package com.raspy.backend.Jwt

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
            // CSRF 비활성화 & 세션 사용 안 함
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

            // URL별 권한 설정
            .authorizeHttpRequests {
                it
                    // 인증 관련 API (로그인/회원가입)
                    .requestMatchers("/api/auth/**").permitAll()
                    // H2 콘솔
                    .requestMatchers("/h2-console/**").permitAll()
                    // OpenAPI 스펙 (JSON, YAML)
                    .requestMatchers(
                        "/v3/api-docs",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml"
                    ).permitAll()
                    // Swagger UI / ReDoc UI
                    .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/doc.html"
                    ).permitAll()
                    // Actuator health 체크
                    .requestMatchers("/actuator/health").permitAll()
                    // 관리자 전용
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    // 나머지 요청은 USER 또는 ADMIN 권한 필요
                    .anyRequest().hasAnyRole("USER", "ADMIN")
            }

            // H2 콘솔을 위한 iframe 허용
            .headers { it.frameOptions { it.sameOrigin() } }

            // JWT 필터 등록
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
