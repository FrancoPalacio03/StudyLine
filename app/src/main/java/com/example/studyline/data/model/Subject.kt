package com.example.studyline.data.model

data class Subject(
    val subjectId: String = "",
    val name: String = "",
    val description: String = "",
    val universityId: String = "",
)

data class SubjectMap(val id: String, val name: String)