package com.example.timecatcher.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.timecatcher.R
import com.example.timecatcher.data.model.UserProfile
import com.example.timecatcher.databinding.ActivityMainDrawerBinding
import com.example.timecatcher.ui.login.LoginActivity
import com.example.timecatcher.ui.main.fragments.HomeFragment
import com.example.timecatcher.ui.main.fragments.MapFragment
import com.example.timecatcher.ui.main.fragments.ProfileFragment
import com.example.timecatcher.ui.main.fragments.SettingsFragment
import com.example.timecatcher.utils.PrefsManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainDrawerBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // Aquí guardamos el correo del usuario (puede ser llenado al hacer login).
    private var userProfile = FirebaseAuth.getInstance().currentUser?.email

    // Para manejo de modo oscuro/claro
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Inicializamos PrefsManager
        prefs = PrefsManager(this)

        // 2) Aplicamos modo oscuro/claro
        if (prefs.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // 3) Inflamos el layout del Drawer con ViewBinding
        binding = ActivityMainDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 4) Configuramos la Toolbar como action bar
        setSupportActionBar(binding.toolbar)

        // 5) Sincronizamos DrawerLayout con el icono de hamburguesa
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // 6) Escuchamos los eventos de la NavigationView
        binding.navView.setNavigationItemSelectedListener(this)

        // 7) AQUI obtén la vista del header y su TextView DESPUÉS de inflar 'binding'
        val headerView = binding.navView.getHeaderView(0)
        val tvEmailHeader = headerView.findViewById<TextView>(R.id.tvNavEmail)

        if (userProfile == null) {
            tvEmailHeader.text = "Desconocido c:"
        }else{
            tvEmailHeader.text = userProfile
        }

        // 8) Cargar un Fragment inicial (HomeFragment) si no hay un estado previo
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
            binding.navView.setCheckedItem(R.id.nav_home)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_profile -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ProfileFragment())
                    .commit()
            }
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, HomeFragment())
                    .commit()
            }
            R.id.nav_map -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MapFragment())
                    .commit()
            }
            R.id.nav_settings -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, SettingsFragment())
                    .commit()
            }
            R.id.nav_logout -> {
                // 1. Cerrar sesión en Firebase
                FirebaseAuth.getInstance().signOut()

                // 2. Limpia cualquier información de userProfile si estás usándola
                userProfile = null

                // 3. Redirige a la pantalla de Login (o cualquier Activity que maneje el inicio de sesión)
                val intent = Intent(this, LoginActivity::class.java)
                // Flags para borrar las Activities anteriores del stack
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
