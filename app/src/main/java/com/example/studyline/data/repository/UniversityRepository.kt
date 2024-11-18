package com.example.studyline.data.repository

import android.net.Uri
import com.example.studyline.data.model.University
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UniversityRepository {
    private val db = FirebaseFirestore.getInstance().collection("university")
    private val fileServer = StorageRepository()

    suspend fun createUniversity(university: University, logoU: Uri) {
        try{
            val url = fileServer.uploadPhotoGeneric("university", university.universityId, logoU)
            if (url != null) {
                university.logo = url
            }
            db.document(university.universityId).set(university).await()
        } catch(e: Exception) {null}
    }
}