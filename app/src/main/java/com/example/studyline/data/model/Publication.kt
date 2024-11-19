package com.example.studyline.data.model

import com.google.firebase.Timestamp


data class  Publication(
    val publicationId: String = "",
    val userId: String = "",
    val subjectId: String = "",
    val commentsId: List<String> = emptyList(), // Colleccion de todos los comentarios asociado a la publicacion
    val fatherPublicationId: String? = null, // Id de la publcacion padre (no es null en caso que sea un comenctario)
    val topic: String = "",
    val description: String? = null,
    val date: Timestamp = Timestamp.now(),
    val files: List<String> = emptyList(),
    val likes : Int = 0,
    val dislikes : Int = 0,
)