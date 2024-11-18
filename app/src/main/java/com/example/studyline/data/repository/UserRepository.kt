package com.example.studyline.data.repository

import android.net.Uri
import android.util.Log
import com.example.studyline.data.model.User
import com.example.studyline.data.repository.PublicationRepositories.CommandPublication
import com.example.studyline.data.repository.PublicationRepositories.QueryPublication
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val db = FirebaseFirestore.getInstance().collection("user")
    private val fileServer = StorageRepository()
    private val publicationQueryRepo = QueryPublication()
    private val publicationCommandRepo = CommandPublication()

    suspend fun registerUser (user: User, photo : Uri?){
        try {
            db.document(user.userId).set(user).await()
            if(photo != null){
                val downloadUrl = fileServer.uploadPhotoGeneric("user", user.userId, photo)
                db.document(user.userId).set(
                    hashMapOf("downloadUrl" to downloadUrl)
                )
            }
        }
        catch(e: Exception){
            Log.e("registerUser", "Failed to register a user", e)}
    }

    suspend fun updateUserInfo (userId: String, downloadUrl: String, fields : Map<String, Any>, newPhoto : Uri?) {
        try {
            val user = db.document(userId)
            fileServer.deletePhotoGeneric("user", userId, downloadUrl)
            if(newPhoto != null){
                val newDownloadUrl = fileServer.uploadPhotoGeneric("user", userId, newPhoto)
                user.update("downloadUrl", newDownloadUrl).await()
            }
            user.update(fields).await()
            user.update("downloadUrl", null).await()

        }catch(e: Exception){
            Log.e("updateUserInfo", "Failed to update a user data", e)}
    }

    suspend fun deleteUser (userId: String, option: Boolean) {
        try {
            val user = db.document(userId)
            val posts = publicationQueryRepo.getPublicationsByUser(userId)
            if(posts != null){
                if(option)
                    publicationCommandRepo.deletePosts(posts)
                else
                    publicationCommandRepo.beAnonymousPublication(posts)
            }
            val downloadUserUrl = user.get().await().getString("downloadUrl")
            if(downloadUserUrl != null)
                fileServer.deletePhotoGeneric("user", userId, downloadUserUrl)
            user.delete().await()

        } catch(e: Exception){
            Log.e("deleteUser", "Failed to delete a user", e)}
    }

    suspend fun getUserById(userId: String) : User? {
        return try {
            val user: DocumentSnapshot = db.document(userId).get().await()
            user.toObject(User::class.java)
        } catch (e: Exception) {
            Log.e("getUserById", "Failed to get the user ${userId}", e)
            null
        }
    }

    suspend fun subscribeToSubject (userId: String, subjectId: String){
        try {
            val userRef = db.document(userId)
            userRef.update("subjectsSubscription", FieldValue.arrayUnion(subjectId)).await()
        } catch (e: Exception) {
            Log.e("subscribeToSubject", "Failed to subscribe to a subject", e)
        }
    }

    suspend fun unsubscribeToSubject (userId: String, subjectId: String){
        try {
            val userRef = db.document(userId)
            userRef.update("subjectsSubscription", FieldValue.arrayRemove(subjectId)).await()
        } catch (e: Exception) {
            Log.e("subscribeToSubject", "Failed to subscribe to a subject", e)
        }
    }
}