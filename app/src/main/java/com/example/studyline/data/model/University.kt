package com.example.studyline.data.model

import android.net.Uri

data class University(
    val universityId: String,
    val name: String,
    val logo: Uri,
    val location: String,
)