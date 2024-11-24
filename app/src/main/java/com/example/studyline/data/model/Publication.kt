    package com.example.studyline.data.model

    import com.google.firebase.Timestamp


    data class  Publication(
        val publicationId: String = "",
        val userId: String = "",
        val subjectId: String = "",
        val commentsId: List<String> = emptyList(), // Colleccion de todos los comentarios asociado a la publicacion
        val fatherPublicationId: String? = null, // Id de la publcacion padre (no es null en caso que sea un comenctario)
        val title: String = "",
        val description: String? = null,
        val date: Timestamp = Timestamp.now(),
        val files: List<String> = emptyList(),
        var likes : Int = 0,
        var dislikes : Int = 0,
        val latitude: Double = 0.0,
        val longitude: Double = 0.0
    )