package com.raspy.backend.user_profile

import com.raspy.backend.user.UserEntity
import com.raspy.backend.user_profile.enumerated.Gender
import com.raspy.backend.user_profile.enumerated.Region
import jakarta.persistence.*

@Entity
@Table(name = "user_profiles")
class UserProfileEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: UserEntity,

    @Column(name = "profile_picture", length = 255)
    var profilePicture: String? = null,  // S3나 CDN URL

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    var region: Region? = null,

    @Column(length = 500)
    var bio: String? = null,

    var age: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(length = 1)
    var gender: Gender? = null
)



