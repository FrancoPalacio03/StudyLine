package com.example.studyline.ui.home

import PhotoUtility
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.studyline.MainActivity
import com.example.studyline.R
import com.example.studyline.data.model.Publication
import com.example.studyline.data.model.SubjectMap
import com.example.studyline.data.repository.PublicationRepositories.CommandPublication
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class CreatePostFragment : Fragment() {

    private lateinit var titleInput: EditText
    private lateinit var subjectSelect: Spinner
    private lateinit var postText: EditText
    private lateinit var uploadButton: Button
    private lateinit var postButton: Button
    private lateinit var selectedFiles: MutableList<Uri>
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var selectedFileText: TextView
    private lateinit var photoUtility: PhotoUtility
    private val REQUEST_CODE_FILE_PICKER = 101
    private val commandPublication = CommandPublication()
    private var selectedSubject: SubjectMap? = null

    private var subjects = listOf(
        SubjectMap("SUB01", "Análisis Matemático I"),
        SubjectMap("SUB02", "Física I"),
        SubjectMap("SUB03", "Algoritmos de Programación"),
        SubjectMap("SUB04", "Química General"),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_post, container, false)
        (activity as? MainActivity)?.hideToolbarAndFab()

        // Inicializar PhotoUtility
        photoUtility = PhotoUtility(requireContext())

        // Vincular vistas
        titleInput = view.findViewById(R.id.title_input)
        subjectSelect = view.findViewById(R.id.testSelect)
        postText = view.findViewById(R.id.post_text)
        uploadButton = view.findViewById(R.id.upload_button)
        postButton = view.findViewById(R.id.post_button)
        selectedFileText = view.findViewById(R.id.selected_file_count)
        loadingSpinner = view.findViewById(R.id.loading_circle)
        selectedFiles = mutableListOf()

        // Configurar spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            subjects.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subjectSelect.adapter = adapter
        subjectSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSubject = subjects[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedSubject = null
            }
        }

        // Configurar botón de carga
        uploadButton.setOnClickListener { openFilePickerOrTakePhoto() }

        // Configurar botón de publicación
        postButton.setOnClickListener { createPost() }

        return view
    }

    private fun openFilePickerOrTakePhoto() {
        val options = arrayOf("Tomar foto", "Seleccionar de la galería")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Seleccionar opción")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> photoUtility.pickImageFromGallery(activity as Activity)
                    1 -> photoUtility.takePhoto(activity as Activity)
                }
            }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoUtility.handleActivityResult(requestCode, resultCode, data) { photoUri ->
            photoUri?.let {
                selectedFiles.add(it)
                selectedFileText.text = "Archivos seleccionados: ${selectedFiles.size}"
            }
        }
    }

    private fun createPost() {
        val title = titleInput.text.toString()
        val description = postText.text.toString()
        val subject = subjectSelect.selectedItem.toString()

        if (title.isBlank() || description.isBlank()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Mostrar la rueda de carga y deshabilitar la pantalla
        loadingSpinner.visibility = View.VISIBLE
        postButton.isEnabled = false
        uploadButton.isEnabled = false

        val postId = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"
        val publication = Publication(
            publicationId = postId,
            userId = userId,
            subjectId = subject,
            title = title,
            description = description
        )

        lifecycleScope.launch {
            // Simula un proceso largo (como subir el post y los archivos)
            commandPublication.createNewPost(publication, selectedFiles)

            // Después de la publicación, se esconde la rueda de carga y habilita la pantalla
            loadingSpinner.visibility = View.GONE
            postButton.isEnabled = true
            uploadButton.isEnabled = true

            // Redirigir al menú principal
            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Publicación realizada exitosamente", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.onBackPressed()  // Vuelve al menú principal
            }
        }
    }
}
