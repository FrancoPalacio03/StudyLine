package com.example.studyline.ui.register

import PhotoUtility
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.studyline.MainActivity
import com.example.studyline.ProviderType
import com.example.studyline.R
import com.example.studyline.data.model.UniversityMap
import com.example.studyline.data.model.User
import com.example.studyline.data.repository.UserRepository
import com.example.studyline.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var photoUtility: PhotoUtility
    private var universities = listOf(
        UniversityMap("UN0", "Soy Autodidacta"),
        UniversityMap("UN1", "Universidad Tecnológica Nacional"),
        UniversityMap("UN2", "Universidad de Buenos Aires"),
        UniversityMap("UN3", "Universidad Nacional Arturo Jauretche"),
    )
    private var selectedUniversity: UniversityMap? = null
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photoUtility = PhotoUtility(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setup()
    }

    private fun setup() {
        title = "Register"

        // Configurar adaptador para universidades
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            universities.map { it.name }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.universitySelect.adapter = adapter

        binding.universitySelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUniversity = universities[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedUniversity = null
            }
        }

        // Configurar clic en ImageView para seleccionar o tomar foto
        binding.imageView.setOnClickListener {
            showImagePickerDialog()
        }

        // Configurar botón de registro
        binding.registerButton.setOnClickListener {
            registerUser()
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Tomar foto", "Seleccionar de la galería")
        AlertDialog.Builder(this)
            .setTitle("Seleccionar imagen")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> photoUtility.takePhoto(this)
                    1 -> photoUtility.pickImageFromGallery(this)
                }
            }
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        photoUtility.handleActivityResult(requestCode, resultCode, data) { uri ->
            selectedImageUri = uri
            if (uri != null) {
                binding.imageView.setImageURI(uri)
            }
        }
    }

    private fun showAlert(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage(message)
        builder.setPositiveButton("Aceptar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(user: String, provider: ProviderType) {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("user", user)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    private fun registerUser() {
        val name = binding.editTextName.text.toString()
        val surname = binding.editTextSurname.text.toString()
        val birthday = binding.editTextDateOfBirth.text.toString()
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()
        val confirmPassword = binding.editTextConfirmPassword.text.toString()

        if (selectedUniversity == null) {
            Toast.makeText(this, "Por favor selecciona una universidad", Toast.LENGTH_SHORT).show()
            return
        }

        val userRepo = UserRepository()

        if (email.isNotEmpty() && password == confirmPassword) {
            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid
                        if (userId != null) {
                            val userData = User(
                                userId = userId,
                                name = "$name $surname",
                                email = email,
                                birthday = birthday,
                                universityId = selectedUniversity!!.id
                            )
                            lifecycleScope.launch {
                                userRepo.registerUser(userData, selectedImageUri)
                            }
                            showHome("$name $surname", ProviderType.BASIC)
                        } else {
                            showAlert("Error al registrar")
                        }
                    } else {
                        showAlert("Error al crear usuario: ${task.exception?.message}")
                    }
                }
        } else {
            showAlert("Las contraseñas no coinciden")
        }
    }
}
