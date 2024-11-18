package com.example.studyline.data.repository.PublicationRepositories

import android.util.Log
import com.example.studyline.data.model.Publication
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot // Representa un conjunto de documentos que resultan de una consulta.
import com.google.firebase.firestore.QuerySnapshot // Representa un conjunto de documentos que resultan de una consulta.
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class QueryPublication {
    private val db = FirebaseFirestore.getInstance().collection("publications")

    suspend fun getPublicationById (publicationId : String) : Publication? {
        return try {
            val documentSnapshot: DocumentSnapshot = db.document(publicationId).get().await()
            documentSnapshot.toObject(Publication::class.java)
        } catch (e: Exception) {
            Log.e("getPublicationById", "Failed to get info for Post ${publicationId}", e)
            null
        }
    }

    suspend fun getPublicationsByUser (userId : String) : List<Publication>?{
        return try {
            val querySnapshot: QuerySnapshot = db.whereEqualTo("userId", userId).get().await()
            querySnapshot.documents.mapNotNull { it.toObject(Publication::class.java) }
        } catch (e: Exception) {
            Log.e("getPublicationsByUser", "Failed to get publication for User ${userId}", e)
            null
        }
    }

    suspend fun getPublicationsBySubject (subjectId : String) : List<Publication>?{
        return try {
            val querySnapshot: QuerySnapshot = db.whereEqualTo("subjectId", subjectId).get().await()
            querySnapshot.documents.mapNotNull { it.toObject(Publication::class.java) }
        } catch (e: Exception) {
            Log.e("getPublicationsBySubject", "Failed to get posts for Subject ${subjectId}", e)
            null
        }
    }
}