package com.example.timecatcher.ui.main.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.timecatcher.databinding.FragmentSettingsBinding
import com.example.timecatcher.utils.PrefsManager

class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var prefsManager: PrefsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsManager = PrefsManager(requireContext())

        // Inicializar los switches con el valor almacenado en SharedPreferences
        binding.switchDarkMode.isChecked = prefsManager.isDarkModeEnabled()
        binding.switchNotifications.isChecked = prefsManager.areNotificationsEnabled()

        // Escuchador para Switch del modo oscuro
        binding.switchDarkMode.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            prefsManager.setDarkModeEnabled(isChecked)
            // Aplicar el cambio de tema
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        // Escuchador para Switch de notificaciones
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.setNotificationsEnabled(isChecked)
            // Aquí podrías habilitar/deshabilitar notificaciones locales o push
        }
    }
}
