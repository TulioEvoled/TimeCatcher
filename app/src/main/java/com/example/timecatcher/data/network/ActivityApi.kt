package com.example.timecatcher.data.network

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.timecatcher.data.model.ActivityItem
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object ActivityApi {

    private const val BASE_URL = "http://10.0.2.2:3000/api/activities"
    // 10.0.2.2 apunta a localhost en el emulador de Android Studio.
    // Cambia si usas dispositivo físico o nube

    /**
     * GET: Obtener todas las actividades
     */
    fun getAllActivities(
        context: Context,
        onSuccess: (List<ActivityItem>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = BASE_URL

        val stringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonArr = JSONArray(response)
                    val activityList = mutableListOf<ActivityItem>()
                    for (i in 0 until jsonArr.length()) {
                        val obj = jsonArr.getJSONObject(i)
                        activityList.add(jsonToActivityItem(obj))
                    }
                    onSuccess(activityList)
                } catch (e: JSONException) {
                    onError("Error parseando JSON: ${e.localizedMessage}")
                }
            },
            Response.ErrorListener { error ->
                onError(error.localizedMessage ?: "Error GET actividades")
            }
        ) {}
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest)
    }

    /**
     * POST: Crear una nueva actividad
     */
    fun createActivity(
        context: Context,
        newItem: ActivityItem,
        onSuccess: (ActivityItem) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = BASE_URL
        val jsonBody = JSONObject().apply {
            put("title", newItem.title)
            put("description", newItem.description)
            put("latitude", newItem.latitude)
            put("longitude", newItem.longitude)
            put("estimatedTime", newItem.estimatedTime)
            put("completed", newItem.completed)
        }

        val request = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                try {
                    val createdItem = jsonToActivityItem(response)
                    onSuccess(createdItem)
                } catch (e: JSONException) {
                    onError("Error parseando JSON: ${e.localizedMessage}")
                }
            },
            Response.ErrorListener { error ->
                onError(error.localizedMessage ?: "Error POST actividad")
            }
        ) {}
        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    /**
     * PUT: Actualizar actividad existente
     */
    fun updateActivity(
        context: Context,
        activityId: String,
        updatedItem: ActivityItem,
        onSuccess: (ActivityItem) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/$activityId"

        val jsonBody = JSONObject().apply {
            put("title", updatedItem.title)
            put("description", updatedItem.description)
            put("latitude", updatedItem.latitude)
            put("longitude", updatedItem.longitude)
            put("estimatedTime", updatedItem.estimatedTime)
            put("completed", updatedItem.completed)
        }

        val request = object : JsonObjectRequest(
            Method.PUT, url, jsonBody,
            Response.Listener { response ->
                try {
                    val updated = jsonToActivityItem(response)
                    onSuccess(updated)
                } catch (e: JSONException) {
                    onError("Error parseando JSON: ${e.localizedMessage}")
                }
            },
            Response.ErrorListener { error ->
                onError(error.localizedMessage ?: "Error PUT actividad")
            }
        ) {}
        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    /**
     * DELETE: Eliminar actividad por ID
     */
    fun deleteActivity(
        context: Context,
        activityId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL/$activityId"
        val request = object : StringRequest(
            Method.DELETE, url,
            Response.Listener { response ->
                // Suponiendo que el backend responde con { "message": "Actividad eliminada" }
                onSuccess()
            },
            Response.ErrorListener { error ->
                onError(error.localizedMessage ?: "Error DELETE actividad")
            }
        ) {}
        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    /**
     * Función auxiliar para convertir JSONObject -> ActivityItem
     * Ajusta según tu modelo y si manejas _id o no
     */
    private fun jsonToActivityItem(json: JSONObject): ActivityItem {
        // Si en tu DB, _id es un String, podrías guardarla en un campo "remoteId" en ActivityItem
        val title = json.optString("title", "")
        val description = json.optString("description", "")
        val latitude = json.optDouble("latitude", 0.0)
        val longitude = json.optDouble("longitude", 0.0)
        val estimatedTime = json.optInt("estimatedTime", 0)
        val completed = json.optBoolean("completed", false)

        // El "id" local (SQLite) es distinto del _id remoto.
        // Podrías crear un campo "remoteId: String?" en tu ActivityItem si lo deseas.
        return ActivityItem(
            id = 0,
            title = title,
            description = description,
            latitude = latitude,
            longitude = longitude,
            estimatedTime = estimatedTime,
            completed = completed
        )
    }
}
