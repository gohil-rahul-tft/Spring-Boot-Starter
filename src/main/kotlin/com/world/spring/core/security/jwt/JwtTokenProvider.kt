package com.world.spring.core.security.jwt


import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*


/**
 * JwtTokenProvider - simple utility to generate and validate JWT tokens.
 * Uses io.jsonwebtoken (jjwt). Secret is base64-encoded string in config.
 *
 * NOTE: For production rotate keys and store securely (KMS / Vault).
 */

@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expiration}") private val expirationMs: Long,
) {
    private val key: Key


    init {
        // Try to decode secret as Base64 first (recommended).
        // If it's not Base64, fall back to using raw bytes of the string.
        val keyBytes = try {
            Decoders.BASE64.decode(secret)
        } catch (ex: IllegalArgumentException) {
            // not a valid base64 string -> use raw bytes (less ideal for HS256 key length)
            secret.toByteArray()
        }
        key = Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(username: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return Jwts.builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun getUsernameFromToken(token: String): String {
        val claims = Jwts.parser()
            .verifyWith(key as javax.crypto.SecretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        return claims.subject
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key as javax.crypto.SecretKey)
                .build()
                .parseSignedClaims(token)
            true
        } catch (ex: Exception) {
            // You can log the exception type here for debugging (ExpiredJwtException, MalformedJwtException, etc.)
            false
        }
    }
}