package com.example.studyline.data.repository

import com.example.studyline.data.model.University
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UniversityRepository {
    private val db = FirebaseFirestore.getInstance().collection("university")
    private val fileServer = StorageRepository()

    suspend fun createUniversity(university: University) {
        try{
            db.document(university.universityId).set(university).await()
            fileServer.uploadPhotoGeneric("university", university.universityId, university.logo)
        } catch(e: Exception) {null}
    }
}