package com.raspy.backend.jwt

import com.raspy.backend.user.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException.Unauthorized
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            log.debug { "JWT token extracted from Authorization header" }

            if (jwtUtil.validateToken(token)) {
                log.debug { "JWT token validated successfully" }



                SecurityContextHolder.getContext().authentication = jwtUtil.getAuthentication(token)
                log.debug { "Authentication set for user: ${jwtUtil.extractEmail(token)}" }
            } else {
                log.warn { "Invalid JWT token received" }
            }
        } else {
            log.debug { "No valid Authorization header found" }
        }

        filterChain.doFilter(request, response)
    }
}
