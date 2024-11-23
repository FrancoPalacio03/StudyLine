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
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task


class CreatePostFragment : Fragment() {

    private lateinit var titleInput: EditText
    private lateinit var subjectSelect: Spinner
    private lateinit var postText: EditText
    private lateinit var uploadButton: Button
    private lateinit var postButton: Button
    private lateinit var selectedFiles: MutableList<Uri>
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var selectedFileText: TextView
    private val REQUEST_CODE_FILE_PICKER = 101
    private val commandPublication = CommandPublication()
    private var selectedSubject: SubjectMap? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentLocation: Pair<Double, Double>? = null // Para almacenar latitud y longitud


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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocationPermissionAndFetchLocation()

        // Vincular vistas
        titleInput = view.findViewById(R.id.title_input)
        subjectSelect = view.findViewById(R.id.testSelect)
        postText = view.findViewById(R.id.post_text)
        uploadButton = view.findViewById(R.id.upload_button)
        postButton = view.findViewById(R.id.post_button)
        selectedFileText = view.findViewById(R.id.selected_file)
        loadingSpinner = view.findViewById(R.id.loading_spinner)
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
                selectedFiles.clear() // Limpiar los archivos previamente seleccionados
                for (i in 0 until it.itemCount) {
                    selectedFiles.add(it.getItemAt(i).uri)
                }
            } ?: data?.data?.let {
                selectedFiles.clear() // Limpiar los archivos previamente seleccionados
                selectedFiles.add(it)
            }

            // Actualizar el texto con la cantidad de archivos seleccionados
            val selectedFileText = "Archivos seleccionados: ${selectedFiles.size}"
            this.selectedFileText.text = selectedFileText
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
        val location = currentLocation ?: Pair(0.0, 0.0) // Valor por defecto si no se obtuvo ubicación

        val publication = Publication(
            publicationId = postId,
            userId = userId,
            subjectId = subject,
            title = title,
            description = description,
            latitude = location.first,
            longitude = location.second
        )

        lifecycleScope.launch {
            commandPublication.createNewPost(publication, selectedFiles)

            loadingSpinner.visibility = View.GONE
            postButton.isEnabled = true
            uploadButton.isEnabled = true

            activity?.runOnUiThread {
                Toast.makeText(requireContext(), "Publicación realizada exitosamente", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.showToolbarAndFab()
                (activity as? MainActivity)?.onBackPressed()
            }
        }
    }


    private fun checkLocationPermissionAndFetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permisos
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1001
            )
            return
        }

        // Obtener ubicación
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = Pair(it.latitude, it.longitude)
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermissionAndFetchLocation()
        } else {
            Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

}