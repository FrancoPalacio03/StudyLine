package com.example.studyline.data.repository

import com.example.studyline.data.model.University
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore

class UniversityRepository {
    private val db = FirebaseFirestore.getInstance().collection("university")

    fun addUniversity(university: University): Task<Void> {
         /* return db.document(university.universityId).set(
            hashMapOf(
                "name" to university.name,
                "logo" to university.logo,
                "location" to university.location
                )
        ) */
        return db.document(university.universityId).set(university)
    }
}