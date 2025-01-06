package com.example.timecatcher.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.timecatcher.data.local.ActivityDAO
import com.example.timecatcher.data.model.ActivityItem
import com.example.timecatcher.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var activityDAO: ActivityDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inicializamos nuestro DAO
        activityDAO = ActivityDAO(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón "Guardar Actividad"
        binding.btnSaveActivity.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (title.isNotEmpty()) {
                val newActivity = ActivityItem(
                    title = title,
                    description = description,
                    latitude = null,
                    longitude = null,
                    estimatedTime = null
                )
                // Insertar en base de datos
                val resultId = activityDAO.insertActivity(newActivity)
                if (resultId > -1) {
                    Toast.makeText(requireContext(), "Actividad insertada con ID: $resultId", Toast.LENGTH_SHORT).show()
                    loadActivities()
                }
            } else {
                Toast.makeText(requireContext(), "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        // Cargar la lista de actividades al iniciar
        loadActivities()
    }

    private fun loadActivities() {
        val list = activityDAO.getAllActivities()
        // Aquí podrías usar un RecyclerView para mostrarlas. Ejemplo rápido:
        binding.tvActivities.text = list.joinToString("\n") {
            "ID:${it.id} - ${it.title}"
        }
    }
}
