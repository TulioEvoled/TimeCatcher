package com.example.timecatcher.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timecatcher.databinding.ActivityRegisterBinding
import com.example.timecatcher.data.model.UserProfile
import com.example.timecatcher.data.network.UserApi
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegisterUser.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val email = binding.etEmailRegister.text.toString().trim()
            val password = binding.etPasswordRegister.text.toString().trim()

            if (fullName.isNotEmpty() && username.isNotEmpty() && description.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(fullName, username, description, email, password)
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(fullName: String, username: String, description: String, email: String, password: String) {
        // 1. Crear cuenta en Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 2. Obtener el uid de Firebase (opcional)
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid

                    // 3. Guardar datos del usuario en el backend (Node.js)
                    //    Pasaremos el email, fullName, username, description
                    val userProfile = UserProfile(
                        fullName = fullName,
                        username = username,
                        description = description,
                        email = email
                    )
                    sendUserToBackend(userProfile)

                } else {
                    // Error en Firebase
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendUserToBackend(userProfile: UserProfile) {
        UserApi.createUserProfile(
            this,
            userProfile,
            onSuccess = {
                Toast.makeText(this, "Registro exitoso, ¡bienvenido ${userProfile.username}!", Toast.LENGTH_SHORT).show()
                // 4. Ir a la pantalla principal o login
                goToMain()
            },
            onError = { errorMsg ->
                Toast.makeText(this, "Error enviando datos: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun goToMain() {
        // Ir a MainActivity o LoginActivity
        val intent = Intent(this, com.example.timecatcher.ui.main.MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}