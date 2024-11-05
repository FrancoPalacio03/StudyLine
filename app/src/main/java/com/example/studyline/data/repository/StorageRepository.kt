package com.example.studyline.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

// averiguar como hacer asincronico los metodos

class StorageRepository {
    val fileServer = FirebaseStorage.getInstance().reference
    
    fun uploadFile (publicationId : String, file : Uri){
        val storageRef= fileServer.child("publications/${publicationId}/files/${file.lastPathSegment}")
        storageRef.putFile(file)
    }

    suspend fun uploadFiles (publicationId : String, files : List<Uri>){
        files.forEach { file ->
            val fileRef = fileServer.child("publications/${publicationId}/files/${file.lastPathSegment}")
            fileRef.putFile(file)
        }
    }

    // suspend son funciones de corrutina para escribir codigo asincronico
    suspend fun getFilesForPublication(publicationId: String) : List<String> {
        val fileUrls = mutableListOf<String>()
        val files : ListResult = fileServer.child("publications/${publicationId}/files/").listAll().await()
        for (file in files.items){
            val url = file.downloadUrl.await().toString()
            fileUrls.add(url)
        }
        return fileUrls
    }

    //se borra asi pq no hay forma en Firebasa Storage de eliminar carpetas
    suspend fun deletePublicationFiles (publicationId : String) : Boolean{
        val publicationFiles = fileServer.child("publications/${publicationId}/files/")
        return try {
            val result = publicationFiles.listAll().await()
            result.items.forEach { fileRef ->
                fileRef.delete().await()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun deleteOneFile (publicationId : String, fileName : String) {
        suspendCancellableCoroutine { continuation ->
            val publicationFile = fileServer.child("publications/${publicationId}/files/${fileName}")
            publicationFile.delete()
                .addOnSuccessListener { continuation.resume(true) }
                .addOnFailureListener { continuation.resume(false) }
        }
    }
}