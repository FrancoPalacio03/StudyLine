package com.example.studyline.data.repository.PublicationRepositories

import android.util.Log
import com.example.studyline.data.model.Comment
import com.example.studyline.data.model.Publication
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot // Representa un conjunto de documentos que resultan de una consulta.
import com.google.firebase.firestore.QuerySnapshot // Representa un conjunto de documentos que resultan de una consulta.
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun getCommentsByPublicationId(publicationId: String): List<Comment> {
        return try {
            val commentsSnapshot = db.document(publicationId)
                .collection("comments")
                .get()
                .await()

            val comments = commentsSnapshot.toObjects(Comment::class.java)
            Log.i("getCommentsByPublicationId", "Successfully fetched comments for $publicationId")
            comments
        } catch (e: Exception) {
            Log.e("getCommentsByPublicationId", "Failed to fetch comments for $publicationId", e)
            emptyList()
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

    suspend fun getRecentPublications () : List<Publication>?{
        return try {
            val querySnapshot: QuerySnapshot = db.orderBy("date", Query.Direction.DESCENDING) // Ordenar por fecha descendente
                .limit(10) // Limitar a 10 resultados
                .get()
                .await() // Esperar los datos
            Log.i("getPublicationsBySubject", "Success to get recent publications")
            querySnapshot.documents.mapNotNull { it.toObject(Publication::class.java) }
        } catch (e: Exception) {
            Log.e("getPublicationsBySubject", "Failed to get recent publications", e)
            null
        }
    }
}