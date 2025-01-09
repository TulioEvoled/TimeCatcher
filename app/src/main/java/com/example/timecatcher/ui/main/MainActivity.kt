package com.example.timecatcher.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.example.timecatcher.R
import com.example.timecatcher.databinding.ActivityMainBinding
import com.example.timecatcher.ui.main.fragments.HomeFragment
import com.example.timecatcher.ui.main.fragments.MapFragment
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import com.example.timecatcher.databinding.ActivityMainDrawerBinding
import com.example.timecatcher.ui.main.fragments.SettingsFragment
import com.example.timecatcher.utils.PrefsManager

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    val prefs = PrefsManager(this)
    private lateinit var binding: ActivityMainDrawerBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {

        if (prefs.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        super.onCreate(savedInstanceState)

        // Usando ViewBinding para activity_main_drawer.xml
        binding = ActivityMainDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar la Toolbar
        setSupportActionBar(binding.toolbar)

        // Crear el toggle para sincronizar el drawer con el icono de hamburguesa
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()  // Muestra el ícono de hamburguesa

        // Escucha de eventos de la NavigationView
        binding.navView.setNavigationItemSelectedListener(this)

        // Abrir un fragment inicial (por ejemplo, HomeFragment)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeFragment())
                .commit()
            binding.navView.setCheckedItem(R.id.nav_home)
        }
    }

    override fun onNavigationItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
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
                // Manejar cierre de sesión (Firebase signOut, etc.)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)  // Cierra el drawer después de la selección
        return true
    }

    override fun onBackPressed() {
        // Si el drawer está abierto, ciérralo, en vez de salir de la app directamente
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }


}

//val fragment = HomeFragment()
//supportFragmentManager.beginTransaction()
//    .replace(R.id.container, fragment)
//    .commit()

// Cargar el MapFragment en el contenedor
//val fragment = MapFragment()
//supportFragmentManager.beginTransaction()
//    .replace(R.id.container, fragment)
//    .commit()