package com.example.timecatcher.ui.main.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.timecatcher.data.local.ActivityDAO
import com.example.timecatcher.data.model.ActivityItem
import com.example.timecatcher.databinding.FragmentHomeBinding
import com.example.timecatcher.utils.FileUtils

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var activityDAO: ActivityDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Crear instancia de nuestro DAO para las operaciones en la BD
        activityDAO = ActivityDAO(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el layout con ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Si vas a poner un Button en el layout, configúralo aquí
        binding.btnExportCsv.setOnClickListener {
            exportActivitiesCSV()
        }

        return binding.root
    }

    private fun exportActivitiesCSV() {
        // 1. Obtener la lista de actividades desde la BD
        val activities: List<ActivityItem> = activityDAO.getAllActivities()

        if (activities.isEmpty()) {
            Toast.makeText(requireContext(), "No hay actividades para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        // 2. Llamar a la función de FileUtils
        val fileUri: Uri? = FileUtils.exportActivitiesToCSV(
            context = requireContext(),
            fileName = "Actividades",   // nombre del archivo sin extensión
            activities = activities
        )

        if (fileUri != null) {
            Toast.makeText(requireContext(), "CSV exportado con éxito", Toast.LENGTH_SHORT).show()

            // 3. Compartir o “Abrir con…” (Opcional)
            shareCSVFile(fileUri)
        } else {
            Toast.makeText(requireContext(), "Error al exportar CSV", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Comparte el archivo CSV con un intent ACTION_SEND.
     */
    private fun shareCSVFile(csvUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, csvUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // permitir que otras apps lean la Uri
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir CSV"))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón para guardar una nueva actividad en la BD
        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()

            if (title.isNotEmpty()) {
                val newActivity = ActivityItem(
                    // No ponemos id porque es autoincrement
                    title = title,
                    description = description,
                    latitude = null,
                    longitude = null,
                    estimatedTime = null,
                    completed = false
                )
                val resultId = activityDAO.insertActivity(newActivity)
                if (resultId > -1) {
                    Toast.makeText(requireContext(), "Insertado ID: $resultId", Toast.LENGTH_SHORT).show()
                    // Limpiar campos
                    binding.etTitle.text.clear()
                    binding.etDescription.text.clear()
                } else {
                    Toast.makeText(requireContext(), "Error al insertar", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Título no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón para cargar todas las actividades
        binding.btnLoadAll.setOnClickListener {
            loadAllActivities()
        }
    }

    private fun loadAllActivities() {
        val list = activityDAO.getAllActivities()
        val textBuilder = StringBuilder()
        list.forEach { activity ->
            textBuilder.append("ID: ${activity.id}\n")
            textBuilder.append("Título: ${activity.title}\n")
            textBuilder.append("Descripción: ${activity.description}\n")
            textBuilder.append("Completado: ${activity.completed}\n\n")
        }

        binding.tvActivities.text = if (textBuilder.isEmpty()) {
            "No hay actividades guardadas."
        } else {
            textBuilder.toString()
        }
    }
}