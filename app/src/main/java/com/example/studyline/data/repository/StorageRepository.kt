package com.example.studyline.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val fileServer = FirebaseStorage.getInstance("gs://studyline-35663.firebasestorage.app").reference


    // UPLOAD TRANSACTIONS
    suspend fun uploadPublicationFile(publicationId: String, file: Uri) : String? {
        return try {
            val storageRef = fileServer.child("publications/${publicationId}/files/${file.lastPathSegment}")
            storageRef.putFile(file).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("uploadPublicationFile", "Failed to upload one file", e)
            null
        }
    }

    suspend fun uploadFileTest(publicationId: String, fileName: String, fileBytes: ByteArray) : String? {
        return try {
            val storageRef = fileServer.child("publications/${publicationId}/files/${fileName}")
            storageRef.putBytes(fileBytes).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            return downloadUrl
        } catch (e: Exception) {
            Log.e("uploadPublicationFile", "Failed to upload one file", e)
            null
        }
    }

    suspend fun uploadPublicationFiles(publicationId: String, files: List<Uri>) : List<String?> {
        // Itera sobre la lista de archivos, los sube, obtiene el URL de descargar y lo mapea en una nueva lista que se retorna
        return files.map { file ->
            try {
                val fileRef = fileServer.child("publications/${publicationId}/files/${file.lastPathSegment}")
                fileRef.putFile(file).await()
                fileRef.downloadUrl.await().toString()
            } catch (e: Exception) {
                Log.e("uploadPublicationFiles", "Failed to upload files", e)
                null
            }
        }
    }

    suspend fun uploadPhotoGeneric (reference : String, id : String, file : Uri) : String?{
        return try {
            val storageRef= fileServer.child("${reference}/${id}/file/${file.lastPathSegment}")
            storageRef.putFile(file).await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e("uploadPhotoGeneric", "Failed to upload the photo in ${reference}", e)
            null
        }
    }

    //DELETE TRANSACTIONS
    suspend fun deletePublicationFiles(publicationId: String) {
        val publicationFiles = fileServer.child("publications/${publicationId}/files/")
        try {
            val result = publicationFiles.listAll().await()
            result.items.forEach { fileRef ->
                fileRef.delete().await()
            }
            Log.i("deletePublicationFiles", "Success to deletes files to publication ${publicationId}")
        } catch (e: Exception) {
            Log.e("deletePublicationFiles", "Failed to deletes files to publication ${publicationId}", e)
        }
    }

    suspend fun deletePhotoGeneric (reference : String, id : String, downloadUrl : String) {
        try {
            val fileResult = fileServer.child("${reference}/${id}/file/${Uri.parse(downloadUrl).lastPathSegment}")
            fileResult.delete().await()
        } catch (e: Exception) {
            Log.e("deletePhotoGeneric", "Failed to delete the photo in ${reference}", e)
        }
    }

}