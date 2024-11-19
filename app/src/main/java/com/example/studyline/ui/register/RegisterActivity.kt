package com.example.studyline.ui.register

import android.content.Intent
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
    private var universities = listOf(
        UniversityMap("UN0", "Soy Autodidacta"),
        UniversityMap("UN1", "Universidad Tecnológica Nacional"),
        UniversityMap("UN2", "Universidad de Buenos Aires"),
        UniversityMap("UN3", "Universidad Nacional Arturo Jauretche"),
    )
    private var selectedUniversity: UniversityMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setup()
    }

    private fun setup() {
        title = "Register"

        // Paso 2: Crear el adaptador mostrando solo los nombres
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            universities.map { it.name } // Mostrar solo los nombres
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Paso 3: Asignar el adaptador al Spinner
        binding.universitySelect.adapter = adapter

        binding.universitySelect.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedUniversity = universities[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedUniversity = null
            }
        }

        binding.registerButton.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val surname = binding.editTextSurname.text.toString()
            val birthday = binding.editTextDateOfBirth.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            if (selectedUniversity == null) {
                Toast.makeText(this, "Por favor selecciona una universidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val userRepo = UserRepository()

            if (email.isNotEmpty() && password == confirmPassword) {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid

                            // Si el UID es válido, crea el objeto User
                            if (userId != null) {
                                val userData = User(
                                    userId = userId,
                                    name = "$name $surname",
                                    email = email,
                                    birthday = birthday,
                                    universityId = selectedUniversity!!.id
                                )
                                lifecycleScope.launch {
                                    userRepo.registerUser(userData, null)
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
}
