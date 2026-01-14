package io.soo.springboot.core.domain

import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException

class RevocationAwareJwtDecoder(
    private val delegate: JwtDecoder,
    private val denylist: JwtDenylistService,
) : JwtDecoder {

    override fun decode(token: String): Jwt {
        val jwt = delegate.decode(token)
        val jti = jwt.id

        if (denylist.isRevoked(jti = jti, tokenValue = token)) {
            throw JwtException("Token revoked")
        }

        return jwt
    }
}