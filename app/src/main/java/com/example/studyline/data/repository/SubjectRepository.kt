package com.example.studyline.data.repository

import android.util.Log
import com.example.studyline.data.model.Subject
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.studyline.data.repository.PublicationRepositories.QueryPublication
import com.example.studyline.data.repository.PublicationRepositories.CommandPublication

class SubjectRepository {
    private val db = FirebaseFirestore.getInstance().collection("subjects")
    private val publicationQueryRepo = QueryPublication()
    private val publicationCommandRepo = CommandPublication()

    suspend fun createSubject (subject : Subject) {
        try {
            db.document(subject.subjectId).set(subject).await()
        }
        catch (e : Exception){
            Log.e("createNewPost", "Failed to create post or upload files", e)
        }
    }

    suspend fun deleteSubject (subjectId : String) {
        try{
            val posts = publicationQueryRepo.getPublicationsBySubject(subjectId)
            if (posts != null) {
                publicationCommandRepo.deletePosts(posts)
            }
            db.document(subjectId).delete().await()
        }
        catch (e : Exception){null}
    }

    suspend fun getSubjectById (subjectId : String) : Subject? {
        return try{
            val documentSnapshot: DocumentSnapshot = db.document(subjectId).get().await()
            documentSnapshot.toObject(Subject::class.java)
        }
        catch (e : Exception){null}
    }
}