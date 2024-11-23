import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import java.io.File

class PhotoUtility(private val context: Context) {

    companion object {
        const val REQUEST_IMAGE_CAPTURE = 1001
        const val REQUEST_IMAGE_PICK = 1002
    }

    var photoUri: Uri? = null

    fun takePhoto(activity: Activity) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        activity.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    fun pickImageFromGallery(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?, onPhotoReady: (Uri?) -> Unit) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val photo = data?.extras?.get("data") as? Bitmap
                    photoUri = saveBitmapToUri(photo)
                    onPhotoReady(photoUri)
                }
                REQUEST_IMAGE_PICK -> {
                    photoUri = data?.data
                    onPhotoReady(photoUri)
                }
            }
        }
    }

    private fun saveBitmapToUri(bitmap: Bitmap?): Uri? {
        if (bitmap == null) return null
        val file = File(context.cacheDir, "temp_image.jpg")
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }
        return Uri.fromFile(file)
    }
}
