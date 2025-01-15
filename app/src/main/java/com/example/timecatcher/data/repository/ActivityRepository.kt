package com.example.timecatcher.data.repository

import android.content.Context
import com.example.timecatcher.data.local.ActivityDAO
import com.example.timecatcher.data.model.ActivityItem
import com.example.timecatcher.data.network.ActivityApi

class ActivityRepository(private val context: Context, private val activityDAO: ActivityDAO) {

    /**
     * Ejemplo: Obtener todas las actividades (unir local y remoto, o escoger una fuente).
     */
    fun getAllActivities(
        onLocalSuccess: (List<ActivityItem>) -> Unit,
        onRemoteSuccess: ((List<ActivityItem>) -> Unit)? = null,
        onError: (String) -> Unit
    ) {
        // 1) Cargar primero local
        val localActivities = activityDAO.getAllActivities()
        onLocalSuccess(localActivities)

        // 2) Opcional: Llamar remoto
        ActivityApi.getAllActivities(
            context,
            onSuccess = { remoteList ->
                // Tal vez quieras mezclar con local o actualizar la DB local
                // Por ejemplo, si deseas sincronizar:
                //  activityDAO.deleteAll()  // si quisieras limpiar local
                //  remoteList.forEach { ... insertActivity(...) }
                onRemoteSuccess?.invoke(remoteList)
            },
            onError = { errorMsg ->
                onError(errorMsg)
            }
        )
    }

    /**
     * Crear nueva actividad en local y/o remoto
     */
    fun createActivity(item: ActivityItem,
                       onLocalSuccess: (Long) -> Unit,
                       onRemoteSuccess: ((ActivityItem) -> Unit)? = null,
                       onError: (String) -> Unit
    ) {
        // 1) Crear localmente
        val newId = activityDAO.insertActivity(item)
        if (newId <= 0) {
            onError("Error al insertar en SQLite")
            return
        }
        onLocalSuccess(newId)

        // 2) Crear remotamente
        ActivityApi.createActivity(
            context,
            item,
            onSuccess = { createdRemote ->
                // Opcionalmente, actualizar local si hay cambios
                onRemoteSuccess?.invoke(createdRemote)
            },
            onError = { error ->
                onError(error)
            }
        )
    }

    /**
     * Actualizar actividad en local + remoto
     */
    fun updateActivity(
        localId: Int,
        remoteId: String, // si manejas un ID remoto
        updatedItem: ActivityItem,
        onLocalSuccess: (Int) -> Unit,
        onRemoteSuccess: ((ActivityItem) -> Unit)? = null,
        onError: (String) -> Unit
    ) {
        // Actualizar en local
        val rows = activityDAO.updateActivity(updatedItem)
        if (rows <= 0) {
            onError("Error al actualizar en SQLite")
            return
        }
        onLocalSuccess(rows)

        // Actualizar en remoto
        ActivityApi.updateActivity(
            context,
            remoteId,
            updatedItem,
            onSuccess = { updatedRemote ->
                onRemoteSuccess?.invoke(updatedRemote)
            },
            onError = { err ->
                onError(err)
            }
        )
    }

    /**
     * Eliminar en local + remoto
     */
    fun deleteActivity(
        localId: Int,
        remoteId: String,
        onLocalSuccess: (Int) -> Unit,
        onRemoteSuccess: (() -> Unit)? = null,
        onError: (String) -> Unit
    ) {
        val rowsDeleted = activityDAO.deleteActivity(localId)
        if (rowsDeleted <= 0) {
            onError("Error al eliminar en SQLite")
            return
        }
        onLocalSuccess(rowsDeleted)

        ActivityApi.deleteActivity(
            context,
            remoteId,
            onSuccess = {
                onRemoteSuccess?.invoke()
            },
            onError = { err ->
                onError(err)
            }
        )
    }

    fun getAllLocalActivities(): List<ActivityItem> {
        return activityDAO.getAllActivities()
    }
}
