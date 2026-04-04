package com.example.recipebook

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.viewModels
import com.example.recipebook.ui.LoadingViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var loadingOverlay: FrameLayout
    private val loadingViewModel: LoadingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadingOverlay = findViewById(R.id.loadingOverlay)
        loadingViewModel.isLoading.observe(this) { isLoading ->
            loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.searchFragment -> {
                    navController.navigate(
                        R.id.searchFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.searchFragment, false)
                            .build()
                    )
                    true
                }

                R.id.profileFragment -> {
                    navController.navigate(
                        R.id.profileFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .build()
                    )
                    true
                }

                R.id.addNavbarFragment -> {
                    navController.navigate(
                        R.id.addNavbarFragment,
                        null,
                        androidx.navigation.NavOptions.Builder()
                            .setPopUpTo(R.id.addNavbarFragment, true)
                            .build()
                    )
                    true
                }

                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.completeProfileFragment -> {
                    bottomNavigation.visibility = View.GONE
                }
                else -> {
                    bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }

    fun showLoading() {
        loadingViewModel.showLoading()
    }

    fun hideLoading() {
        loadingViewModel.hideLoading()
    }
}