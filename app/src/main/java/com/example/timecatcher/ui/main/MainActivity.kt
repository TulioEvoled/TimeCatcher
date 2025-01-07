package com.example.timecatcher.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.timecatcher.R
import com.example.timecatcher.databinding.ActivityMainBinding
import com.example.timecatcher.ui.main.fragments.HomeFragment


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Aquí luego implementaremos la navegación
        binding.textViewWelcome.text = "¡Bienvenido a TimeCatcher!"

        val fragment = HomeFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()

        // Cargar el MapFragment en el contenedor
        //val fragment = MapFragment()
        //supportFragmentManager.beginTransaction()
        //    .replace(R.id.container, fragment)
        //    .commit()
    }


}