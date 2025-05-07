package com.raspy.backend.jwt

import com.raspy.backend.user.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserPrincipal(
    val id: Long,
    val email: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>
) : UserDetails {

    companion object {
        fun fromUser(user: UserEntity): UserPrincipal {
            return UserPrincipal(
                id = user.id!!,
                email = user.email,
                password = user.password,
                authorities = user.roles.map { it.toGrantedAuthority() }
            )
        }
    }

    override fun getAuthorities() = authorities
    override fun getPassword() = password
    override fun getUsername() = email
    override fun isAccountNonExpired() = true
    override fun isAccountNonLocked() = true
    override fun isCredentialsNonExpired() = true
    override fun isEnabled() = true
}