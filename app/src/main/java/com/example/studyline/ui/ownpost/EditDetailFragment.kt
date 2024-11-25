package com.example.studyline.ui.ownpost

import android.Manifest
import android.app.AlertDialog
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
import com.example.studyline.databinding.FragmentEditDetailBinding
import com.example.studyline.databinding.FragmentPostDetailBinding
import com.example.studyline.ui.home.adapters.CommentsAdapter
import com.example.studyline.utils.MapsUtility
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.File
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale


class EditDetailFragment : Fragment() {

    private var _binding: FragmentEditDetailBinding? = null
    private val binding get() = _binding!!
    private val commandPublication = CommandPublication()
    private val queryPublication = QueryPublication()
    private var publicationId: String? = null
    private lateinit var mapsUtility : MapsUtility
    val currentUser = FirebaseAuth.getInstance().currentUser
    private val REQUEST_WRITE_STORAGE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDetailBinding.inflate(inflater, container, false)
        mapsUtility = MapsUtility(requireContext(), lifecycleScope)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar el ID de la publicación
        publicationId = arguments?.getString(ARG_PUBLICATION_ID)

        // Cargar detalles de la publicación y sus comentarios
        viewLifecycleOwner.lifecycleScope.launch {
            publicationId?.let { id ->
                loadPostDetails(id)
            } ?: showSnackbar("ID de la publicación no proporcionado.")
        }


        binding.editTitleButton.setOnClickListener {
            val idField = binding.postDetailTitle
            val idString = getString(R.string.edit_post_text)
            lifecycleScope.launch{
                editField(requireContext(), idField, idString, "title",publicationId.toString())
            }
            Toast.makeText(requireContext(), requireContext().getString(R.string.edit_title_result), Toast.LENGTH_SHORT).show()
        }

        binding.editSubjectButton.setOnClickListener {
            val idField = binding.postDetailSubject
            val idString = getString(R.string.edit_post_subject)
            lifecycleScope.launch{
                editField(requireContext(), idField, idString, "subjectId", publicationId.toString())
            }
            Toast.makeText(requireContext(), requireContext().getString(R.string.edit_subject_result), Toast.LENGTH_SHORT).show()

        }

        binding.editDescriptionButton.setOnClickListener {
            val idField = binding.postDetailDescription
            val idString = getString(R.string.edit_post_description)
            lifecycleScope.launch{
                editField(requireContext(), idField, idString, "description", publicationId.toString())
            }
            Toast.makeText(requireContext(), requireContext().getString(R.string.edit_description_result), Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun editField (
        context : Context,
        idField: EditText,
        idString : String,
        fieldUpdate: String,
        publicationId: String)
    {
        val inputEditText = EditText(context).apply {
            setText(idField.text.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(idString)
            .setView(inputEditText)
            .setPositiveButton(getString(R.string.save_world)) { _, _ ->
                idField.setText(inputEditText.text.toString())
                val newValue = inputEditText.text.toString()
                lifecycleScope.launch{
                    commandPublication.updatePublicationByField(publicationId, fieldUpdate, newValue)
                }
            }
            .setNegativeButton(getString(R.string.cancel_world), null)
            .show()
    }

    private suspend fun loadPostDetails(postId: String) {

        try {
            val publication = queryPublication.getPublicationById(postId)
            if (publication != null) {
                val date: com.google.firebase.Timestamp = publication.date
                val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateParse = formatter.format(date.toDate())
                binding.postDetailTitle.setText(publication.title)
                binding.postDetailDate.text = dateParse
                binding.postDetailSubject.setText(publication.subjectId)
                binding.postDetailDescription.setText(publication.description)
                binding.postDetailUsename.text = currentUser?.email
                binding.postCountLike.text = publication.likes.toString()
                binding.postCountDislike.text = publication.dislikes.toString()
                binding.postCountComment.text = publication.commentsId.size.toString()
                mapsUtility.getAddressFromCoordinates(publication.latitude, publication.longitude) { address ->
                    binding.postDetailLocation.text = address ?: requireContext().getString(R.string.address_not_available)
                }


                loadFiles(publication.files)

            } else {
                showSnackbar("La publicación no se encontró.")
            }
        } catch (e: Exception) {
            showSnackbar("Error al cargar los detalles de la publicación.")
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

        fun newInstance(publicationId: String): EditDetailFragment {
            return EditDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PUBLICATION_ID, publicationId)
                }
            }
        }
    }

    private fun loadFiles(files: List<String>) {
        val filesLayout = binding.filesRecyclerView

        // Manejo de la visibilidad según si hay archivos o no
        if (files.isEmpty()) {
            filesLayout.visibility = View.GONE // Oculta la RecyclerView
            binding.filesLabel.text = requireContext().getString(R.string.not_file_publication) // Cambia el texto del label
        } else {
            filesLayout.visibility = View.VISIBLE // Muestra la RecyclerView

            // Iterar y añadir los archivos a la RecyclerView
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

                // Añadir el icono al layout
                fileItemLayout.addView(fileIcon)

                // Agregar el layout a la vista principal
                filesLayout.addView(fileItemLayout)
            }
        }
    }


    private fun getFileExtension(fileUrl: String): String {
        return fileUrl.substring(fileUrl.lastIndexOf(".") + 1).lowercase(Locale.getDefault())
    }

}
