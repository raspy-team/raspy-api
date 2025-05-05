package com.raspy.backend.User

import jakarta.persistence.*

@Entity
@Table(name = "users")
class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false, length = 50)
    var email: String,

    @Column(nullable = false, length = 80)  // 보안을 위해 해시된 비밀번호는 길이를 넉넉히 확보 (예: bcrypt는 60자)
    var password: String,

    @Column(nullable = false, length = 30)
    var nickname: String,

    @ElementCollection(fetch = FetchType.EAGER, targetClass = Role::class)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Enumerated(EnumType.STRING)
    var roles: Set<Role> = setOf(Role.ROLE_USER)
)
