package io.soo.springboot.core.domain

import io.soo.springboot.storage.db.core.LocalCredentialRepository
import io.soo.springboot.storage.db.core.UserAccountRepository
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    private val localCredentialRepository: LocalCredentialRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserPrincipal? {
        val email = username.trim().lowercase()
        val user = userAccountRepository.findByEmail(email)
            ?: throw UsernameNotFoundException("User not found by email: $email")

        val cred = user.id?.let { localCredentialRepository.findByUserId(it) }
            ?: throw UsernameNotFoundException("Local credential not found: userId=${user.id}")

        return user.id?.let {
            UserPrincipal(
                userId = it,
                email = email,
                passwordHash = cred.passwordHash,
            )
        }
    }
}
