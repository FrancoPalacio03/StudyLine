package com.example.studyline.ui.home

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studyline.R
import com.example.studyline.data.model.Comment
import com.example.studyline.data.model.Publication
import com.example.studyline.data.repository.PublicationRepositories.CommandPublication
import com.example.studyline.data.repository.PublicationRepositories.QueryPublication
import com.example.studyline.databinding.FragmentPostDetailBinding
import com.example.studyline.ui.home.adapters.CommentsAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale


class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null
    private val binding get() = _binding!!

    private val commandPublication = CommandPublication()
    private val queryPublication = QueryPublication()
    private var publicationId: String? = null
    private val REQUEST_WRITE_STORAGE = 100

    private lateinit var commentsAdapter: CommentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar RecyclerView para comentarios
        commentsAdapter = CommentsAdapter()
        binding.commentsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = commentsAdapter
        }

        // Recuperar el ID de la publicación
        publicationId = arguments?.getString(ARG_PUBLICATION_ID)

        // Cargar detalles de la publicación y sus comentarios
        viewLifecycleOwner.lifecycleScope.launch {
            publicationId?.let { id ->
                loadPostDetails(id)
            } ?: showSnackbar("ID de la publicación no proporcionado.")
        }

        // Agregar nuevo comentario
        binding.addCommentButton.setOnClickListener {
            val commentText = binding.commentInput.text.toString().trim()
            if (commentText.isNotEmpty() && publicationId != null) {
                addNewComment(commentText)
            } else {
                showSnackbar("El comentario no puede estar vacío.")
            }
        }
    }

    private suspend fun loadPostDetails(postId: String) {

        try {
            val publication = queryPublication.getPublicationById(postId)
            if (publication != null) {
                val date: com.google.firebase.Timestamp = publication.date
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateParse = formatter.format(date.toDate())
                binding.postDetailTitle.text = publication.title
                binding.postDetailDate.text = dateParse
                binding.postDetailSubject.text = publication.subjectId
                binding.postDetailDescription.text = publication.description
                binding.postCountLike.text = publication.likes.toString()
                binding.postCountDislike.text = publication.dislikes.toString()

                loadFiles(publication.files)

                loadComments(postId)
            } else {
                showSnackbar("La publicación no se encontró.")
            }
        } catch (e: Exception) {
            showSnackbar("Error al cargar los detalles de la publicación.")
        }


    }

    private suspend fun loadComments(postId: String) {
        try {
            val comments = queryPublication.getCommentsByPublicationId(postId)
            commentsAdapter.submitList(comments)
        } catch (e: Exception) {
            showSnackbar("Error al cargar los comentarios.")
        }
    }

    private fun addNewComment(content: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val newComment = Comment(
                    userId = currentUser?.email,
                    publicationId = publicationId!!,
                    content = content,
                    date = System.currentTimeMillis()
                )
                commandPublication.createNewComment(publicationId!!, newComment)
                binding.commentInput.text.clear()
                binding.commentInput.clearFocus()
                commentsAdapter.addComment(newComment)
                showSnackbar("Comentario añadido.")


            } catch (e: Exception) {
                showSnackbar("Error al añadir el comentario.")
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PUBLICATION_ID = "publicationId"

        fun newInstance(publicationId: String): PostDetailFragment {
            return PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PUBLICATION_ID, publicationId)
                }
            }
        }
    }

    private fun loadFiles(files: List<String>) {
        val filesLayout = binding.filesLayout
        files.forEach { fileUrl ->
            val fileExtension = getFileExtension(fileUrl)

            // Crear contenedor para el icono y el botón de descarga
            val fileItemLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(16, 16, 16, 16)
            }

            // Icono de vista previa
            val fileIcon = ImageView(requireContext()).apply {
                val iconResId = when {
                    fileExtension in listOf("jpg", "jpeg", "png", "gif") -> R.drawable.ic_image
                    fileExtension == "pdf" -> R.drawable.ic_pdf
                    fileExtension in listOf("txt", "doc", "docx") -> R.drawable.ic_text_file
                    else -> R.drawable.ic_generic_file
                }
                setImageResource(iconResId)
                layoutParams = ViewGroup.LayoutParams(100, 100) // Tamaño pequeño
            }

            // Botón de descarga
            val downloadButton = Button(requireContext()).apply {
                text = "Descargar"
                setOnClickListener { downloadFile(fileUrl) }
            }

            // Añadir el icono y el botón al layout
            fileItemLayout.addView(fileIcon)
            fileItemLayout.addView(downloadButton)

            // Agregar el layout a la vista principal
            filesLayout.addView(fileItemLayout)
        }
    }

    private fun getFileExtension(fileUrl: String): String {
        return fileUrl.substring(fileUrl.lastIndexOf(".") + 1).lowercase(Locale.getDefault())
    }





    private fun downloadFile(fileUrl: String) {
        if (checkStoragePermission()) {
            val downloadManager =
                requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val uri = Uri.parse(fileUrl)
            val request = DownloadManager.Request(uri).apply {
                setTitle("Descargando archivo")
                setDescription("Archivo relacionado con la publicación.")
                setDestinationInExternalPublicDir(
                    "/Download/publications", File(uri.path).name
                )
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            }
            downloadManager.enqueue(request)
        } else {
            showSnackbar("Se requiere permiso de almacenamiento para descargar archivos.")
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_STORAGE
            )
            false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, proceder con la operación
            } else {
                showSnackbar("Permiso de almacenamiento denegado.")
            }
        }
    }
}
