package com.example.studyline.data.model

import android.net.Uri

data class University(
    val universityId: String = "",
    val name: String = "",
    var logo: String? = null,
    val location: String? = null,
    val latitude: String? = null,
    val logitude: String? = null
)

data class UniversityMap(val id: String, val name: String)