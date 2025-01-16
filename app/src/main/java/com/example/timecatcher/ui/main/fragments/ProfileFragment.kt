package com.example.timecatcher.ui.main.fragments

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.timecatcher.R
import com.example.timecatcher.data.model.UserProfile
import com.example.timecatcher.data.network.UserApi
import com.example.timecatcher.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private var currentUserProfile: UserProfile? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Recuperar el correo del usuario de Firebase, por ejemplo
        val email = FirebaseAuth.getInstance().currentUser?.email
        if (email != null) {
            // 2. Pedir al backend sus datos de perfil
            fetchUserProfile(email)
        } else {
            Toast.makeText(requireContext(), "No hay usuario logueado", Toast.LENGTH_SHORT).show()
        }

        // 3. Botón guardar
        binding.btnSaveProfile.setOnClickListener {
            saveProfileChanges()
        }
    }

    private fun fetchUserProfile(email: String) {
        UserApi.getUserProfileByEmail(
            requireContext(),
            email,
            onSuccess = { profile ->
                currentUserProfile = profile
                // Rellenar la UI
                binding.etProfileFullName.setText(profile.fullName)
                binding.etProfileUsername.setText(profile.username)
                binding.etProfileDescription.setText(profile.description)
                binding.etProfileEmail.setText(profile.email)
                // `profile.remoteId` tiene el _id del backend
            },
            onError = { err ->
                Toast.makeText(requireContext(), "Error: $err", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveProfileChanges() {
        val fullName = binding.etProfileFullName.text.toString().trim()
        val username = binding.etProfileUsername.text.toString().trim()
        val description = binding.etProfileDescription.text.toString().trim()
        val email = binding.etProfileEmail.text.toString().trim()

        // Validar
        if (fullName.isEmpty() || username.isEmpty() || description.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Actualizar el objeto actual
        val updatedProfile = currentUserProfile?.copy(
            fullName = fullName,
            username = username,
            description = description
        ) ?: UserProfile(
            // Si no existe currentUserProfile, lo creas, pero usualmente ya vendrá de fetch
            remoteId = null, // Sin ID no se podrá actualizar
            fullName = fullName,
            username = username,
            description = description,
            email = email
        )

        // Llamar a la API de update
        UserApi.updateUserProfile(
            requireContext(),
            updatedProfile,
            onSuccess = {
                Toast.makeText(requireContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show()
            },
            onError = { err ->
                Toast.makeText(requireContext(), "Error: $err", Toast.LENGTH_SHORT).show()
            }
        )
    }
}