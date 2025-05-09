package com.raspy.backend.jwt

import com.raspy.backend.user.UserRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import java.util.*
import javax.crypto.SecretKey

private val log = KotlinLogging.logger {}

@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long,
    private val userRepository: UserRepository
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(userId: Long, username: String, roles: List<String>): String {
        val now = Date()
        val expiry = Date(now.time + expiration)

        log.debug { "Generating JWT for user: $username (ID: $userId)" }

        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getAuthentication (token: String): Authentication {
        val userId = extractUserId(token)
        log.debug { "Extracted user ID from token: $userId" }

        val userEntity = userRepository.findById(userId)
            .orElseThrow {
                log.warn { "User not found with ID: $userId" }
                IllegalArgumentException("User not found with id $userId")
            }

        val userPrincipal = UserPrincipal.fromUser(userEntity)
        return UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.authorities
        )
    }

    fun validateToken(token: String): Boolean {
        return try {
            log.debug { "Validating JWT token" }
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            true
        } catch (ex: Exception) {
            log.warn { "Invalid JWT token: ${ex.message}" }
            false
        }
    }

    fun extractUserId(token: String): Long {
        log.debug { "Extracting user ID from JWT" }
        return Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .body
            .get("userId", Integer::class.java)
            .toLong()
    }

    fun extractEmail(token: String): String {
        log.debug { "Extracting username from JWT" }
        return Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .body
            .subject
    }

    fun extractRoles(token: String): List<String> {
        log.debug { "Extracting roles from JWT" }
        return Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .body
            .get("roles", List::class.java)
            .map { it.toString() }
    }
}
