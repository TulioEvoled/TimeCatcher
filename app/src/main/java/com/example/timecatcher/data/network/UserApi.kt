package com.example.timecatcher.data.network

import android.content.Context
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.example.timecatcher.data.model.UserProfile
import org.json.JSONException
import org.json.JSONObject

object UserApi {

    private const val BASE_URL = "http://10.0.2.2:3000/api/users"
    // Ajusta a tu IP o dominio si tu Node está en la nube

    /**
     * Crear usuario en el backend (POST /api/users)
     */
    fun createUserProfile(
        context: Context,
        userProfile: UserProfile,
        onSuccess: (UserProfile) -> Unit,  // ahora devolvemos el perfil con remoteId
        onError: (String) -> Unit
    ) {
        val url = BASE_URL
        val jsonBody = JSONObject().apply {
            put("fullName", userProfile.fullName)
            put("username", userProfile.username)
            put("description", userProfile.description)
            put("email", userProfile.email)
        }

        val request = object : JsonObjectRequest(
            Method.POST, url, jsonBody,
            Response.Listener { response ->
                try {
                    val createdUser = UserProfile(
                        remoteId = response.optString("_id", null),
                        fullName = response.optString("fullName", ""),
                        username = response.optString("username", ""),
                        description = response.optString("description", ""),
                        email = response.optString("email", "")
                    )
                    onSuccess(createdUser)
                } catch (e: JSONException) {
                    onError("Error parseando JSON: ${e.localizedMessage}")
                }
            },
            Response.ErrorListener { error ->
                onError(error.localizedMessage ?: "Error POST usuario")
            }
        ) {}
        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }

    fun getUserProfileByEmail(
        context: Context,
        email: String,
        onSuccess: (UserProfile) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "$BASE_URL?email=$email"
        val stringRequest = object : StringRequest(
            Method.GET, url,
            Response.Listener { response ->
                try {
                    val jsonObj = JSONObject(response)
                    val userProfile = UserProfile(
                        // si en el backend se llama "_id", lo obtienes con obj.getString("_id")
                        remoteId = jsonObj.optString("_id", null),
                        fullName = jsonObj.optString("fullName", ""),
                        username = jsonObj.optString("username", ""),
                        description = jsonObj.optString("description", ""),
                        email = jsonObj.optString("email", "")
                    )
                    onSuccess(userProfile)
                } catch (e: JSONException) {
                    onError("Error parseando JSON: ${e.localizedMessage}")
                }
            },
            Response.ErrorListener { error ->
                onError(error.localizedMessage ?: "Error GET user profile")
            }
        ) {}
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest)
    }

    fun updateUserProfile(
        context: Context,
        updatedProfile: UserProfile,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val userId = updatedProfile.remoteId ?: run {
            onError("No se tiene el ID remoto para actualizar")
            return
        }

        val url = "$BASE_URL/$userId"
        val jsonBody = JSONObject().apply {
            put("fullName", updatedProfile.fullName)
            put("username", updatedProfile.username)
            put("description", updatedProfile.description)
            // email no se actualiza
        }

        val request = object : JsonObjectRequest(
            Method.PUT, url, jsonBody,
            Response.Listener { response ->
                // parse si deseas ver el objeto devuelto
                onSuccess()
            },
            Response.ErrorListener { error ->
                onError(error.localizedMessage ?: "Error actualizando user profile")
            }
        ) {}
        VolleySingleton.getInstance(context).addToRequestQueue(request)
    }
}