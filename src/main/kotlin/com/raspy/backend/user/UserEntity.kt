package com.raspy.backend.user

import com.raspy.backend.user.enumerated.Role
import com.raspy.backend.user_profile.UserProfileEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false, length = 50)
    var email: String,

    @Column(nullable = false, length = 80)
    var password: String,

    @Column(nullable = false, length = 30)
    var nickname: String,

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Role::class)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    var roles: Set<Role> = setOf(Role.ROLE_USER),

    /**
     * Lazy fetch
     */
    @OneToOne(mappedBy = "user", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, optional = false)
    var profile: UserProfileEntity? = null
)
