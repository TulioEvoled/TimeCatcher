package com.example.timecatcher.ui.main.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.timecatcher.data.local.ActivityDAO
import com.example.timecatcher.data.model.ActivityItem
import com.example.timecatcher.data.repository.ActivityRepository
import com.example.timecatcher.databinding.FragmentHomeBinding
import com.example.timecatcher.utils.FileUtils

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var repository: ActivityRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Instanciamos el DAO
        val dao = ActivityDAO(requireContext())
        // 2) Creamos el repository (recibe el dao y el context)
        repository = ActivityRepository(requireContext(), dao)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflar el layout con ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Botón para exportar CSV
        binding.btnExportCsv.setOnClickListener {
            exportActivitiesCSV()
        }

        // Botón para guardar una nueva actividad (LOCAL + REMOTO a través del Repository)
        binding.btnSave.setOnClickListener {
            saveNewActivity()
        }

        // Botón para cargar todas las actividades (ejemplo de integración local + remoto)
        binding.btnLoadAll.setOnClickListener {
            loadAllActivities()
        }

        return binding.root
    }

    /**
     * Ejemplo de crear una nueva actividad usando el Repository.
     * Se creará localmente y luego en el backend.
     */
    private fun saveNewActivity() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()

        if (title.isNotEmpty()) {
            val newActivity = ActivityItem(
                // id autoincrement local, lo asignará SQLite.
                title = title,
                description = description,
                latitude = null,
                longitude = null,
                estimatedTime = null,
                completed = false
            )

            repository.createActivity(
                item = newActivity,
                onLocalSuccess = { newId ->
                    // Esto se llama cuando la inserción local fue exitosa
                    Toast.makeText(requireContext(), "Insertado local con ID: $newId", Toast.LENGTH_SHORT).show()
                    // Limpiar campos
                    binding.etTitle.text.clear()
                    binding.etDescription.text.clear()
                },
                onRemoteSuccess = { remoteItem ->
                    // Esto se llama cuando la creación en el servidor fue exitosa
                    Toast.makeText(requireContext(), "Creado en servidor: ${remoteItem.title}", Toast.LENGTH_SHORT).show()
                },
                onError = { errorMsg ->
                    // Manejo de error para remoto/local
                    Toast.makeText(requireContext(), "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(requireContext(), "Título no puede estar vacío", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Cargar actividades (local + remoto) usando el Repository.
     */
    private fun loadAllActivities() {
        // Llamamos a un método del repositorio que obtenga primero local, luego remoto
        repository.getAllActivities(
            onLocalSuccess = { localList ->
                // Mostrar la lista local en la UI
                showActivitiesInTextView(localList)
            },
            onRemoteSuccess = { remoteList ->
                // Si deseas sobreescribir la lista con la remota o fusionar,
                // aquí podrías decidir tu lógica. Ej.: mostrar en el mismo TextView
                // o sincronizar la DB local con la remota
                // showActivitiesInTextView(remoteList)
                Toast.makeText(requireContext(), "Datos remotos cargados: ${remoteList.size} items", Toast.LENGTH_SHORT).show()
            },
            onError = { err ->
                Toast.makeText(requireContext(), "Error: $err", Toast.LENGTH_SHORT).show()
            }
        )
    }

    /**
     * Muestra una lista de ActivityItem en el TextView.
     */
    private fun showActivitiesInTextView(list: List<ActivityItem>) {
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

    /**
     * Exportar CSV (igual que antes, usando el FileUtils).
     */
    private fun exportActivitiesCSV() {
        // Si deseas exportar SOLO lo local (SQLite)
        val localActivities = repository.getAllLocalActivities()  // Podrías crear un método "getAllLocalActivities()" en tu Repository

        if (localActivities.isEmpty()) {
            Toast.makeText(requireContext(), "No hay actividades para exportar", Toast.LENGTH_SHORT).show()
            return
        }

        val fileUri: Uri? = FileUtils.exportActivitiesToCSV(
            context = requireContext(),
            fileName = "Actividades",
            activities = localActivities
        )

        if (fileUri != null) {
            Toast.makeText(requireContext(), "CSV exportado con éxito", Toast.LENGTH_SHORT).show()
            shareCSVFile(fileUri)
        } else {
            Toast.makeText(requireContext(), "Error al exportar CSV", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareCSVFile(csvUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, csvUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Compartir CSV"))
    }
}
