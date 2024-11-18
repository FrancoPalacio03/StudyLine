package com.example.studyline.data.repository.PublicationRepositories

import android.net.Uri
import android.util.Log
import com.example.studyline.data.model.Publication
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.example.studyline.data.repository.StorageRepository
import com.example.studyline.data.repository.UserRepository
import kotlinx.coroutines.tasks.await

class CommandPublication {
    private val db = FirebaseFirestore.getInstance().collection("publications")
    private val fileServer = StorageRepository()

    // CREATE FUNCTIONS
    suspend fun createNewPost (post : Publication, files : List<Uri>?) {
        try {
            db.document(post.publicationId).set(post).await()
            if(files != null){
                val downloadURLs = fileServer.uploadPublicationFiles(post.publicationId, files)
                downloadURLs.let { urls ->
                    // arrayUnion(*urls.toTypedArray()) actualiza la lista en una sola llamada de Update
                    db.document(post.publicationId).update("files", FieldValue.arrayUnion(*urls.toTypedArray()))
                }
            }
            Log.i("createNewPost", "Successfully to create the post ${post.publicationId}")
        } catch (e: Exception) {
            Log.e("createNewPost", "Failed to create post ${post.publicationId}", e)
        }
    }

    suspend fun createNewComment (originalPostId : String, comment : Publication) {
        try {
            val originalPost = db.document(originalPostId)
            db.document(comment.publicationId).set(comment).await()
            originalPost.update("commentsId", FieldValue.arrayUnion(comment.publicationId)).await()
            Log.i("createNewComment", "Successfully to create the comment ${comment.publicationId}")
        } catch (e: Exception) {
            Log.e("createNewComment", "Failed to create the comment ${comment.publicationId}", e)
        }
    }

    // DELETE FUNCTIONS
    suspend fun deletePostById (postId : String) {
        try {
            val postRef = db.document(postId)
            val files = postRef.get().await().get("files") as List<String>
            val commentsForPost = postRef.get().await().get("commentsId") as List<String>
            if (files.isNotEmpty())
                fileServer.deletePublicationFiles(postId)
            if(commentsForPost.isNotEmpty()){
                commentsForPost.forEach { commentId ->
                    val commentRef = db.document(commentId)
                    commentRef.delete().await()
                }
            }
            db.document(postId).delete().await()
            Log.i("deletePostById", "Successfully to deleted the post ${postId}")
        } catch (e: Exception) {
            Log.e("deletePostById", "Failed to delete post ${postId}", e)
        }
    }

    suspend fun deleteCommentById (comment : Publication) {
        try {
            val commentRef = db.document(comment.publicationId)
            val document = commentRef.get().await()
            val publicationId = document.getString("fatherPublicationId")
            val originalPostRef = db.document(publicationId.toString())
            originalPostRef.update("commentsId", FieldValue.arrayRemove(comment.publicationId)).await()
            if(comment.files.isNotEmpty())
                fileServer.deletePublicationFiles(comment.publicationId)
            commentRef.delete().await()
        } catch (e: Exception) {
            Log.e("deleteCommentById", "Failed to delete comment", e)
        }
    }
        suspend fun deletePosts (posts : List<Publication>) {
            posts.forEach { post ->
                db.document(post.publicationId).delete().await()
                fileServer.deletePublicationFiles(post.publicationId)
            }
        }

    // UPDATE FUNCTIONS
    suspend fun <T> updatePublicationByField(publicationId : String, field : String, value : T){
        try {
            val publication = db.document(publicationId)
            if (value is List<*>) {
                publication.update(field, FieldValue.arrayUnion(value)).await()
            } else {
                publication.update(field, value).await()
            }
            Log.d("updatePublicationByField", "Update Field Success")
        } catch (e: Exception) {
            Log.e("updatePublicationByField", "Failed to update field", e)
        }
    }

    suspend fun updatePublication(publicationId : String, fields : Map<String, Any>){
        try {
            val publication = db.document(publicationId)
            publication.update(fields).await()
            Log.d("updatePublication", "Update Field Success")
        } catch (e: Exception) {
            Log.e("updatePublication", "Failed to update field", e)
        }
    }

    suspend fun beAnonymousPublication (publications : List<Publication>) {
        try {
            publications.forEach { publication ->
                val publicationRef = db.document(publication.publicationId)
                publicationRef.update("userId", "anonymousUser").await()
            }
        } catch (e: Exception) {Log.e("beAnonymousPublication", "Failed to be anonymous the publications", e)}
    }

    suspend fun likePublicationForId (publicationId: String) {
        try {
            db.document(publicationId).update("likes", FieldValue.increment(1)).await()
        } catch (e: Exception) {Log.e("likePublicationForId", "Failed to increment the like of publications", e)}
    }

    suspend fun dislikePublicationForId (publicationId: String) {
        try {
            db.document(publicationId).update("likes", FieldValue.increment(-1)).await()
        } catch (e: Exception) {Log.e("dislikePublicationForId", "Failed to decrement the like of publications", e)}
    }
}