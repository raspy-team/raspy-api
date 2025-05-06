package com.raspy.backend.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expiration: Long
) {
    private val key: SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(userId: Long, username: String, roles: List<String>): String {
        val now = Date()
        val expiry = Date(now.time + expiration)
        return Jwts.builder()
            .setSubject(username)
            .claim("userId", userId)
            .claim("roles", roles)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }


    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
            true
        } catch (ex: Exception) {
            ex.printStackTrace()
            false
        }
    }

    fun extractUserId(token: String): Long =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .body
            .get("userId", Integer::class.java)
            .toLong()

    fun extractUsername(token: String): String =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .body
            .subject

    fun extractRoles(token: String): List<String> =
        Jwts.parserBuilder().setSigningKey(key).build()
            .parseClaimsJws(token)
            .body
            .get("roles", List::class.java)
            .map { it.toString() }
}
