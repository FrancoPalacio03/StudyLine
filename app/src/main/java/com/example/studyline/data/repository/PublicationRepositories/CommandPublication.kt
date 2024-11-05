package com.example.studyline.data.repository.PublicationRepositories

import android.util.Log
import com.example.studyline.data.model.Publication
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CommandPublication {
    private val db = FirebaseFirestore.getInstance().collection("publications")

    // CREATE FUNCTIONS
    fun createNewPost (post : Publication) {
        db.document(post.publicationId).set(post)
    }

    fun createNewComment (originalPostId : String, comment : Publication) {
        val originalPost  = db.document(originalPostId)
        db.document(comment.publicationId).set(comment)
            .addOnSuccessListener {
                originalPost.update("commentsId", FieldValue.arrayUnion(comment.publicationId))
            }.addOnFailureListener {
                Log.e("createNewComment", "Failed to add comment")
            }
    }

    // DELETE FUNCTIONS
    fun deletePostById (post : Publication) {
        db.document(post.publicationId).delete()
    }

    fun deleteCommentById (comment : Publication) {
        val commentRef  = db.document(comment.publicationId)
        commentRef.get().addOnSuccessListener { document ->
            val publicationId = document.getString("fatherPublicationId")
            commentRef.delete()
        }.addOnFailureListener {
            Log.e("deleteCommentById", "Failed to delete commnet")
        }
    }

    // UPDATE FUNCTIONS
    fun <T> updatePublicationByField(publicationId : String, field : String, value : T){
        val publication = db.document(publicationId)
        if(value is List<*>){
            publication.update(field, FieldValue.arrayUnion(value)).addOnSuccessListener{
                Log.d("updatePublicationByField", "Update Field Success")
            }.addOnFailureListener {
                Log.e("updatePublicationByField", "Failed to update field")
            }
        }
        else{
            publication.update(field, value).addOnSuccessListener{
                Log.d("updatePublicationByField", "Update Field Success")
            }.addOnFailureListener {
                Log.e("updatePublicationByField", "Failed to update field")
            }
        }
    }

    fun updatePublication(publicationId : String, fields : Map<String, Any>){
        val publication = db.document(publicationId)
        publication.update(fields).addOnSuccessListener{
            Log.d("updatePublication", "Update Field Success")
        }.addOnFailureListener {
            Log.e("updatePublication", "Failed to update field")
        }
    }
}