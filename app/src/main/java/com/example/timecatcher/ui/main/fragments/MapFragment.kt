package com.example.timecatcher.ui.main.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.timecatcher.R
import com.example.timecatcher.data.local.ActivityDAO
import com.example.timecatcher.data.model.ActivityItem
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var activityDAO: ActivityDAO

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Instanciar el DAO para operaciones en la BD
        activityDAO = ActivityDAO(requireContext())

        // 2. Obtener la referencia al MapFragment y configurar el callback
        val mapFragment = childFragmentManager.findFragmentById(R.id.googleMapFragment)
                as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()

        // 3. Manejar el evento de long press para crear actividad
        map.setOnMapLongClickListener { latLng ->
            showCreateActivityDialog(latLng)
        }

        // 4. Manejo de clic en InfoWindow (opcional)
        map.setOnInfoWindowClickListener { marker ->
            val activityId = marker.tag as? Int ?: return@setOnInfoWindowClickListener
            showUpdateDeleteDialog(activityId)
            // Aquí podrías, por ejemplo, mostrar un Toast o abrir un DetailActivity
            Toast.makeText(requireContext(), "Marker: ${marker.title}", Toast.LENGTH_SHORT).show()
        }

        // 5. Cargar los markers al iniciar
        loadMarkers()
    }

    /**
     * Solicita permisos de ubicación y habilita la capa MyLocation del mapa.
     */
    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            map.isMyLocationEnabled = true
            centerMapOnUserLocation()
        }
    }

    /**
     * Centra el mapa en la ubicación del usuario (si disponible).
     */
    private fun centerMapOnUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))

                    // Ejemplo opcional: agregar un marker
                    map.addMarker(
                        MarkerOptions().position(userLatLng).title("Estás aquí")
                    )
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


    /**
     * Crear un marker por cada actividad guardada en la BD.
     */
    private fun loadMarkers() {
        map.clear() // limpiar markers previos
        val activities = activityDAO.getAllActivities()
        for (activity in activities) {
            if (activity.latitude != null && activity.longitude != null) {
                val position = LatLng(activity.latitude, activity.longitude)
                val markerOptions = MarkerOptions()
                    .position(position)
                    .title(activity.title)
                    .snippet(activity.description)
                // Guardamos el Marker que se crea al añadirlo al mapa
                val marker = map.addMarker(markerOptions)

                // Aquí asignamos la ID de la actividad al tag del marker
                marker?.tag = activity.id
            }
        }
    }

    /**
     * Diálogo para ingresar título/descr de la actividad al hacer long press en el mapa.
     */
    private fun showCreateActivityDialog(latLng: LatLng) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_activity, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)

        AlertDialog.Builder(requireContext())
            .setTitle("Crear Actividad")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()

                if (title.isNotEmpty()) {
                    val newActivity = ActivityItem(
                        title = title,
                        description = description,
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        estimatedTime = null,
                        completed = false
                    )
                    val resultId = activityDAO.insertActivity(newActivity)
                    if (resultId > 0) {
                        loadMarkers()
                        Toast.makeText(requireContext(), "Actividad creada", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Título requerido", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun showUpdateDeleteDialog(activityId: Int) {
        // 1. Obtener la actividad actual de la BD
        val activityItem = activityDAO.getActivityById(activityId) ?: return

        // 2. Inflar el layout del diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_update_delete, null)
        val etTitleUpdate = dialogView.findViewById<EditText>(R.id.etTitleUpdate)
        val etDescriptionUpdate = dialogView.findViewById<EditText>(R.id.etDescriptionUpdate)

        // 3. Rellenar los EditText con los datos actuales
        etTitleUpdate.setText(activityItem.title)
        etDescriptionUpdate.setText(activityItem.description)

        // 4. Construir el AlertDialog
        AlertDialog.Builder(requireContext())
            .setTitle("Editar Actividad")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { dialog, _ ->
                // Actualizar la actividad
                val newTitle = etTitleUpdate.text.toString().trim()
                val newDesc = etDescriptionUpdate.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    val updatedItem = activityItem.copy(
                        title = newTitle,
                        description = newDesc
                    )
                    val rowsAffected = activityDAO.updateActivity(updatedItem)
                    if (rowsAffected > 0) {
                        Toast.makeText(requireContext(), "Actualizado", Toast.LENGTH_SHORT).show()
                        loadMarkers()  // refrescar mapa
                    }
                } else {
                    Toast.makeText(requireContext(), "Título requerido", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNeutralButton("Eliminar") { dialog, _ ->
                // Eliminar la actividad
                val rowsDeleted = activityDAO.deleteActivity(activityId)
                if (rowsDeleted > 0) {
                    Toast.makeText(requireContext(), "Eliminado", Toast.LENGTH_SHORT).show()
                    loadMarkers() // refrescar mapa
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }



    /**
     * Maneja el resultado de solicitar permisos al usuario.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, habilita la ubicación
                enableMyLocation()
            } else {
                // Permiso denegado, podrías mostrar un mensaje o deshabilitar funciones
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }
}
