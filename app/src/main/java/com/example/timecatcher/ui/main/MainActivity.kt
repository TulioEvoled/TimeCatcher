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

    private lateinit var binding: ActivityMainDrawerBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // 1) Declaramos 'prefs' como lateinit para inicializarla correctamente en onCreate().
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2) Ahora inicializamos 'prefs' con un contexto válido (this).
        prefs = PrefsManager(this)

        // 3) Aplicamos modo oscuro/claro según la preferencia guardada.
        if (prefs.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        // 4) Inflamos el layout del Drawer.
        binding = ActivityMainDrawerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 5) Configuramos la Toolbar
        setSupportActionBar(binding.toolbar)

        // 6) Sincronizamos el DrawerLayout con el icono de hamburguesa en la Toolbar
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        // 7) Escucha de eventos del NavigationView
        binding.navView.setNavigationItemSelectedListener(this)

        // 8) Abrimos un fragment inicial (por ejemplo, HomeFragment).
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

//val fragment = HomeFragment()
//supportFragmentManager.beginTransaction()
//    .replace(R.id.container, fragment)
//    .commit()

// Cargar el MapFragment en el contenedor
//val fragment = MapFragment()
//supportFragmentManager.beginTransaction()
//    .replace(R.id.container, fragment)
//    .commit()