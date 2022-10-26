package com.udacity.chatto

import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp

import com.udacity.chatto.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val client by lazy { LocationServices.getFusedLocationProviderClient(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.lifecycleOwner=this
        val viewmodel=ViewModelProvider(this).get(MainViewmodel::class.java)
        binding.viewmodel=viewmodel

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener{
            controler,destination,_->
            run {
                supportActionBar?.apply {
                     if(destination.id==R.id.homeFragment){
                         show()
                         setDisplayShowHomeEnabled(false)
                         setDisplayHomeAsUpEnabled(false)
                         setHomeButtonEnabled(false)
                         setDisplayShowTitleEnabled(false)
                     }
                    else if(destination.id==R.id.signinFragment){
                         hide()
                    }
                    else{
                         show()
                         setDisplayShowHomeEnabled(true)
                         setDisplayHomeAsUpEnabled(true)
                         setHomeButtonEnabled(true)
                         setDisplayShowTitleEnabled(true)
                    }
                }
            }

        }
    }



}