package com.raspy.backend.user.enumerated

import org.springframework.security.core.GrantedAuthority

enum class Role {
    ROLE_USER,
    ROLE_ADMIN;

    fun toGrantedAuthority(): GrantedAuthority {
        return GrantedAuthority { name }
    }
}