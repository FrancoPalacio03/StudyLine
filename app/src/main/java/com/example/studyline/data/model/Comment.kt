package com.example.studyline.data.model

data class Comment(
    val commentId: String = "",
    val userId: String? = "" ,
    val publicationId: String = "",
    val content: String = "",
    val date: Long = 0 // Fecha como timestamp
)
