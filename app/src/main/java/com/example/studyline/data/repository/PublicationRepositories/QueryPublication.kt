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

    suspend fun getPublicationByIdParse (publicationId : String) : Publication? {
        return try {
            val documentSnapshot = db.document(publicationId).get().await()
            documentSnapshot.toObject(Publication::class.java)
        } catch (e: Exception) { null }
        // Manda el objeto ya mapeado, pero al usarlo hay que implementar Corrutinas al ser asincronico
        // import kotlinx.coroutines.*
    }

    fun getPublicationById (publicationId : String) : Task<DocumentSnapshot> {
        return db.document(publicationId).get()
        // Manda el objeto de tipo Task, y se debe mapear el objeto donde se llame para usarlo
        // documentSnapshot.toObject(Publication::class.java)
    }

    fun getPublicationsByUser (userId : String) : Task<QuerySnapshot>{
        return db.whereEqualTo("userId", userId).get()
        // querySnapshot.documents.mapNotNull { it.toObject(Publication::class.java) }
    }
}