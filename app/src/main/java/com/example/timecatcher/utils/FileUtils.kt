package com.example.timecatcher.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.timecatcher.data.model.ActivityItem
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    /**
     * Crea un archivo CSV con la lista de actividades y devuelve su Uri, o null si falla.
     *
     * @param context El contexto para acceder a los archivos.
     * @param fileName El nombre base del archivo (sin extensión). Por ejemplo "Actividades".
     * @param activities Lista de actividades a exportar.
     * @return Uri del archivo CSV creado, o null si ocurre algún error.
     */
    fun exportActivitiesToCSV(
        context: Context,
        fileName: String,
        activities: List<ActivityItem>
    ): Uri? {
        return try {
            // 1. Crear archivo en el directorio de archivos de la app
            //    En getExternalFilesDir(null) NO se necesita permiso de WRITE_EXTERNAL_STORAGE
            //    A partir de Android 10+, este es un espacio "privado" de la app en memoria externa.

            val csvFile = File(context.getExternalFilesDir(null), "$fileName.csv")

            // Escribimos el CSV
            FileOutputStream(csvFile).use { fos ->
                fos.write("ID,Titulo,Descripcion,Completado\n".toByteArray())
                for (act in activities) {
                    val row = buildString {
                        append(act.id).append(",")
                        append("\"${act.title}\",")
                        append("\"${act.description}\",")
                        append(act.completed)
                        append("\n")
                    }
                    fos.write(row.toByteArray())
                }
            }

            // Generamos una URI de tipo content:// en vez de file://
            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",  // Fíjate en el Manifest
                csvFile
            )

            contentUri

        } catch (e: Exception) {
            Log.e("FileUtils", "Error al exportar CSV: ${e.localizedMessage}", e)
            null
        }
    }
}
