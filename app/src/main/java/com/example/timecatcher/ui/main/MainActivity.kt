package com.example.timecatcher.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timecatcher.R
import com.example.timecatcher.databinding.ActivityMainBinding
import com.example.timecatcher.ui.main.fragments.MapFragment
import androidx.lifecycle.lifecycleScope
import com.example.timecatcher.data.model.ActivityItem
import com.example.timecatcher.data.repository.ActivityRepository
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var repository: ActivityRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = ActivityRepository(this)
        // Botón de ejemplo para insertar un ActivityItem
        binding.btnInsertActivity.setOnClickListener {
            lifecycleScope.launch {
                val newItem = ActivityItem(
                    title = "Visitar cafetería retro",
                    description = "Disfruta un café vintage",
                    latitude = 20.676,
                    longitude = -103.347,
                    estimatedTime = 30,
                    completed = false
                )
                repository.addActivity(newItem)
                Toast.makeText(this@MainActivity, "Actividad insertada", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón de ejemplo para leer actividades
        binding.btnGetActivities.setOnClickListener {
            lifecycleScope.launch {
                val allActivities = repository.getAllActivities()
                // Actualiza la UI, por ejemplo imprimir en logs
                allActivities.forEach {
                    Log.d("MainActivity", "Activity: ${it.title}, completed: ${it.completed}")
                }
            }
        }

        // Aquí luego implementaremos la navegación
        binding.textViewWelcome.text = "¡Bienvenido a TimeCatcher!"

        // Cargar el MapFragment en el contenedor
        val fragment = MapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }

}