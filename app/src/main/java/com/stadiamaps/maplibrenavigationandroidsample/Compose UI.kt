package com.stadiamaps.maplibrenavigationandroidsample

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

@Composable
fun NavigationMap() {
    var destination: Point? by remember { mutableStateOf(null) }
    var mapboxMap: MapboxMap? by remember { mutableStateOf(null) }
    var navigationMapRoute: NavigationMapRoute? by remember { mutableStateOf(null) }
    var route: DirectionsRoute? by remember { mutableStateOf(null) }

    AndroidView(factory = { context ->
        Mapbox.getInstance(context)

        val mapView = MapView(context)
        mapView.getMapAsync { map ->
            mapboxMap = map

            // Gets the style URL. Assumes that TestNavigationViewLight
            // is a style in styles.xml and it has in item navigationViewMapStyle
            val styleUri = getStringAttributeFromStyle(
                context, R.style.TestNavigationViewLight, R.attr.navigationViewMapStyle
            )!!

            map.setStyle(Style.Builder().fromUri(styleUri)) { style ->
                navigationMapRoute = NavigationMapRoute(
                    mapView, map
                )

                map.locationComponent.let {
                    // Activate with a built LocationComponentActivationOptions object
                    it.activateLocationComponent(
                        LocationComponentActivationOptions.builder(context, style).build(),
                    )

                    // Enable to make component visible
                    if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        it.isLocationComponentEnabled = true
                    }

                    // Set the component's camera mode
                    it.cameraMode = CameraMode.TRACKING_GPS_NORTH

                    // Set the component's render mode
                    it.renderMode = RenderMode.NORMAL
                }
            }

            map.addOnMapClickListener { point ->
                when {
                    destination == null -> {
                        destination =
                            Point.fromLngLat(point.longitude, point.latitude)
                        map.addMarker(MarkerOptions().position(point))
                    }
                }

                val userLocation = map.locationComponent.lastKnownLocation
                val dest = destination
                if (userLocation != null && dest != null) {
                    val origin = Point.fromLngLat(
                        userLocation.longitude, userLocation.latitude
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val routes = getDirections(listOf(origin, dest))

                            withContext(Dispatchers.Main) {
                                if (routes.isEmpty()) {
                                    Toast.makeText(context, "No routes found", Toast.LENGTH_LONG)
                                        .show()
                                } else {
                                    route = routes.first()
                                    navigationMapRoute?.addRoutes(routes)
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Log.e("router", e.localizedMessage)
                                Toast.makeText(context, "Error fetching routes", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                }

                true
            }
        }

        mapView
    })

    if (destination != null) {
        route?.let {
            StartRouteControls(route = it)
        }

        FloatingActionButton(onClick = {
            mapboxMap?.markers?.forEach {
                mapboxMap?.removeMarker(it)
            }

            destination = null
            route = null

            navigationMapRoute?.updateRouteVisibilityTo(false)
            navigationMapRoute?.updateRouteArrowVisibilityTo(false)
        }) {
            Text(text = "Clear Points")
        }
    }
}

@Composable
fun StartRouteControls(route: DirectionsRoute) {
    var simulateRoute by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context.findActivity()!!

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Row(
            modifier = Modifier
                .background(
                    Color(0xFF121212), shape = RoundedCornerShape(4.dp)
                ) // Your colorPrimaryDark
                .padding(8.dp)
        ) {
            // Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Simulate Route", color = Color.White
                )

                Switch(checked = simulateRoute, onCheckedChange = { simulateRoute = it })
            }

            // Button
            Button(
                onClick = {
                    val initialCamera =
                        route.legs()!!.first().steps()!!.first().maneuver().location()
                    val options = NavigationLauncherOptions.builder()
                        .directionsRoute(route)
                        .shouldSimulateRoute(simulateRoute).initialMapCameraPosition(
                            CameraPosition.Builder().target(
                                LatLng(
                                    initialCamera.latitude(), initialCamera.longitude()
                                )
                            ).build()
                        )
                        .lightThemeResId(R.style.TestNavigationViewLight)
                        .darkThemeResId(R.style.TestNavigationViewDark)
                        // This relies on implementation details of Mapbox styles at the moment
                        .waynameChipEnabled(false)
                        .build()

                    NavigationLauncher.startNavigation(activity, options)
                },
            ) {
                Text(text = "Start Route")
            }
        }
    }
}
