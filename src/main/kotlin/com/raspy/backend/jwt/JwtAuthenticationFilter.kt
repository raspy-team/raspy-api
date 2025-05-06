package com.raspy.backend.jwt

import com.raspy.backend.user.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtUtil: JwtUtil,
    private val userRepository: UserRepository  // DB에서 UserEntity 조회용
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.substring(7)
            if (jwtUtil.validateToken(token)) {
                val userId = jwtUtil.extractUserId(token)
                val userEntity = userRepository.findById(userId)
                    .orElseThrow { IllegalArgumentException("User not found with id $userId") }

                val userPrincipal = UserPrincipal.fromUser(userEntity)

                val auth = UsernamePasswordAuthenticationToken(
                    userPrincipal,  // principal로 UserPrincipal 넣기
                    null,
                    userPrincipal.authorities
                )
                SecurityContextHolder.getContext().authentication = auth
            }
        }
        filterChain.doFilter(request, response)
    }
}
