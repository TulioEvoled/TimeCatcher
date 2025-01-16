package com.example.timecatcher.ui.register

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.timecatcher.databinding.ActivityLoginBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aquí puedes manejar las interacciones de los elementos de tu registro
    }
}
