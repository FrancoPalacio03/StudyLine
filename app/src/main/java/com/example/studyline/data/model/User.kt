package com.example.studyline.data.model

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val birthday: String = "",
    val downloadUrl: String? = null,
    val universityId: String = "",
    val likePercentage : Int? = 0,
    val subjectsSubscription: List<String?> = emptyList()
)