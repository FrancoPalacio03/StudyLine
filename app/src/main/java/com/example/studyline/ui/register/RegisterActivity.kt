package com.example.studyline.ui.register

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.studyline.MainActivity
import com.example.studyline.ProviderType
import com.example.studyline.R
import com.example.studyline.data.model.User
import com.example.studyline.data.repository.UserRepository
import com.example.studyline.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

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

        binding.registerButton.setOnClickListener {
            val name = binding.editTextName.text.toString()
            val surname = binding.editTextSurname.text.toString()
            val university = binding.editTextUniversity.text.toString()
            val birthday = binding.editTextDateOfBirth.text.toString()
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            val userData = User (
                userId = "user1",
                name = "$name $surname",
                email = email,
                birthday = birthday,
                universityId = "Uni1"
            )
            val userRepo = UserRepository()

            if (email.isNotEmpty() && password == confirmPassword) {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            lifecycleScope.launch {
                                userRepo.registerUser(userData, null)
                            }
                            showHome(email, ProviderType.BASIC)
                        } else {
                            showAlert("Error al registrar")
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

    private fun showHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, MainActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }
}
