package com.example.timecatcher.ui.main.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timecatcher.data.local.ActivityDAO
import com.example.timecatcher.data.model.ActivityItem
import com.example.timecatcher.data.model.InProgressItem
import com.example.timecatcher.data.network.ActivityApi
import com.example.timecatcher.data.repository.ActivityRepository
import com.example.timecatcher.databinding.FragmentHomeBinding
import com.example.timecatcher.ui.home.adapters.InProgressAdapter
import com.example.timecatcher.ui.home.adapters.SuggestionsAdapter
import com.example.timecatcher.utils.FileUtils
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var suggestionsAdapter: SuggestionsAdapter
    private lateinit var inProgressAdapter: InProgressAdapter
    private val inProgressList = mutableListOf<InProgressItem>()

    private lateinit var repository: ActivityRepository

    private lateinit var activityDAO: ActivityDAO

    // Handler para refrescar cada segundo
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            // Forzamos que se re-renderice el inProgressAdapter (onBindViewHolder)
            inProgressAdapter.notifyDataSetChanged()

            // Volvemos a programar en 1 segundo
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Instanciar el DAO para operaciones en la BD
        activityDAO = ActivityDAO(requireContext())
        // 2) Creamos el repository (recibe el dao y el context)
        repository = ActivityRepository(requireContext(), activityDAO)

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar RecyclerView horizontal de sugerencias
        binding.rvSuggestions.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        suggestionsAdapter = SuggestionsAdapter(emptyList()) { activity ->
            // Al pulsar "Realizar"
            startActivityInProgress(activity)
        }
        binding.rvSuggestions.adapter = suggestionsAdapter

        // Configurar RecyclerView vertical de "en progreso"
        binding.rvInProgress.layoutManager = LinearLayoutManager(requireContext())
        inProgressAdapter = InProgressAdapter(inProgressList) { inProgressItem ->
            markActivityCompleted(inProgressItem)
        }
        binding.rvInProgress.adapter = inProgressAdapter

        // Botón "Buscar" (ingresa tiempo disponible y filtra)
        binding.btnSearchActivities.setOnClickListener {
            val timeStr = binding.etAvailableTime.text.toString().trim()
            if (timeStr.isNotEmpty()) {
                val userTime = timeStr.toInt()
                fetchAndFilterActivities(userTime)
            }
        }

        // Iniciar el refresco del temporizador
        handler.post(updateRunnable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Parar el handler
        handler.removeCallbacks(updateRunnable)
    }

    private fun fetchAndFilterActivities(userTime: Int) {
        getUserLocation { lat, lon ->
            // 1) Local
            val localList = activityDAO.getAllActivities()
            // 2) Remoto
            ActivityApi.getAllActivities(
                requireContext(),
                onSuccess = { remoteList ->
                    val combined = localList + remoteList
                    val filtered = filterActivities(combined, userTime, lat, lon)
                    val top3 = filtered.take(3)
                    // Actualiza el adapter de sugerencias
                    suggestionsAdapter = SuggestionsAdapter(top3) { activity ->
                        startActivityInProgress(activity)
                    }
                    binding.rvSuggestions.adapter = suggestionsAdapter
                },
                onError = { errorMsg ->
                    Toast.makeText(requireContext(), "Error: $errorMsg", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun getUserLocation(callback: (latitude: Double, longitude: Double) -> Unit) {

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    callback(it.latitude, it.longitude)
                }
            }
        } else {
            // Si los permisos no están concedidos, solicítalos
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    private fun filterActivities(
        allActivities: List<ActivityItem>,
        userTime: Int,
        userLat: Double,
        userLon: Double
    ): List<ActivityItem> {
        val results = mutableListOf<ActivityItem>()
        for (act in allActivities) {
            val actTime = act.estimatedTime ?: 0
            if (actTime <= userTime) {
                // comprobar distancia
                val dist = calculateDistance(userLat, userLon, act.latitude ?: 0.0, act.longitude ?: 0.0)
                if (dist <= 5000) { // 5 km
                    results.add(act)
                }
            }
        }
        return results
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] // metros
    }

    /**
     * Al pulsar "Realizar" en una tarjeta de sugerencia.
     * Creamos un InProgressItem con finishTimeMillis = now + (activity.estimatedTime * 60 * 1000).
     * Agregamos a la lista y refrescamos rvInProgress.
     */
    private fun startActivityInProgress(activity: ActivityItem) {
        val estimated = activity.estimatedTime ?: 0
        val start = System.currentTimeMillis()
        val finish = start + (estimated * 60 * 1000L)

        val inProg = InProgressItem(
            activity = activity,
            startTimeMillis = start,
            finishTimeMillis = finish
        )
        inProgressList.add(inProg)
        inProgressAdapter.notifyDataSetChanged()
    }

    /**
     * Al pulsar "Completado" en la lista "en progreso".
     * Marcamos la actividad como completada en SQLite, y removemos el item en progreso.
     */
    private fun markActivityCompleted(inProgressItem: InProgressItem) {
        // 1) Actualizar en DB local
        val updated = inProgressItem.activity.copy(completed = true)
        activityDAO.updateActivity(updated)

        // 2) Quitar de la lista inProgress
        inProgressList.remove(inProgressItem)
        inProgressAdapter.notifyDataSetChanged()

        Toast.makeText(requireContext(), "¡Actividad Completada!", Toast.LENGTH_SHORT).show()
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
