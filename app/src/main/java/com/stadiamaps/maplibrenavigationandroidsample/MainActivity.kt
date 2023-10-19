package com.stadiamaps.maplibrenavigationandroidsample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.stadiamaps.maplibrenavigationandroidsample.ui.theme.MaplibreNavigationAndroidSampleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var permissionGranted by remember {
                mutableStateOf(isPermissionGranted())
            }

            // Launcher for requesting location permissions
            val permissionLauncher =
                rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { success ->
                    // this is called when the user selects allow or deny
                    permissionGranted = success
                }

            MaplibreNavigationAndroidSampleTheme {
                if (permissionGranted) {
                    // Show the navigation map if we have location services available
                    NavigationMap()
                } else {
                    // Otherwise, provide a way to request location permissions
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Button(
                                enabled = !permissionGranted,
                                onClick = {
                                    if (!permissionGranted) {
                                        // ask for permission
                                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }) {
                                Text(text = if (permissionGranted) "Permission Granted" else "Request Location Permissions")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun isPermissionGranted(): Boolean = ActivityCompat.checkSelfPermission(
        this, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}
