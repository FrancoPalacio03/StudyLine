package com.example.studyline.ui.home

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.studyline.MainActivity
import com.example.studyline.R
import com.example.studyline.data.model.Publication
import com.example.studyline.data.model.SubjectMap
import com.example.studyline.data.repository.PublicationRepositories.CommandPublication
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.*

class CreatePostFragment : Fragment() {

    private lateinit var titleInput: EditText
    private lateinit var subjectSelect: Spinner
    private lateinit var postText: EditText
    private lateinit var uploadButton: Button
    private lateinit var postButton: Button
    private lateinit var selectedFiles: MutableList<Uri>
    private val REQUEST_CODE_FILE_PICKER = 101
    private val commandPublication= CommandPublication()
    private var selectedSubject: SubjectMap? = null

    private var subjects = listOf(
        SubjectMap("SUB01", "Análisis Matemático I"),
        SubjectMap("SUB02", "Física I"),
        SubjectMap("SUB03", "Algoritmos de Programación"),
        SubjectMap("SUB04", "Química GeneraL"),
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_create_post, container, false)
        (activity as? MainActivity)?.hideToolbarAndFab()
        // Vincular vistas
        titleInput = view.findViewById(R.id.title_input)
        subjectSelect = view.findViewById(R.id.testSelect)
        postText = view.findViewById(R.id.post_text)
        uploadButton = view.findViewById(R.id.upload_button)
        postButton = view.findViewById(R.id.post_button)
        selectedFiles = mutableListOf()


        // Configurar spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            subjects.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subjectSelect.adapter = adapter
        subjectSelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSubject = subjects[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedSubject = null
            }
        }

        // Configurar botón de carga
        uploadButton.setOnClickListener { openFilePicker() }

        // Configurar botón de publicación
        postButton.setOnClickListener { createPost() }

        return view
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, REQUEST_CODE_FILE_PICKER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_FILE_PICKER && resultCode == Activity.RESULT_OK) {
            data?.clipData?.let {
                for (i in 0 until it.itemCount) {
                    selectedFiles.add(it.getItemAt(i).uri)
                }
            } ?: data?.data?.let { selectedFiles.add(it) }
            Toast.makeText(requireContext(), "Archivos seleccionados: ${selectedFiles.size}", Toast.LENGTH_SHORT).show()
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

        val postId = UUID.randomUUID().toString()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "Unknown"
        val publication = Publication(
            publicationId = postId,
            userId = userId,
            subjectId = subject,
            title = title,
            description = description,
        )


        lifecycleScope.launch {
            commandPublication.createNewPost(publication, selectedFiles)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.showToolbarAndFab()
    }
}
