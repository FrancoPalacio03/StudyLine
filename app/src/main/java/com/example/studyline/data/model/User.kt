package com.example.studyline.data.model

data class User(
    val userId: String,
    val name: String,
    val email: String,
    val photo: String? = null,
    val universityId: String,
    val likePercentage : Float,
    val subjectsSubscription: List<String> = emptyList()
)