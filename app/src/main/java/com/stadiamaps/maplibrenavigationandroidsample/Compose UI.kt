package com.stadiamaps.maplibrenavigationandroidsample

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncher
import com.mapbox.services.android.navigation.ui.v5.NavigationLauncherOptions
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NavigationMap() {
    var waypoints: List<Point> by remember { mutableStateOf(listOf()) }
    var mapboxMap: MapboxMap? by remember { mutableStateOf(null) }
    var navigationMapRoute: NavigationMapRoute? by remember { mutableStateOf(null) }
    var route: DirectionsRoute? by remember { mutableStateOf(null) }
    var asx: Boolean = true

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

            map.addOnMapClickListener {

                                   // point ->
                //waypoints += Point.fromLngLat(point.longitude, point.latitude)
                //map.addMarker(MarkerOptions().position(point))
                asx = true


                //val originPoint = Point.fromLngLat(  -47.0101979,   -23.162793)
                //val originPoint = Point.fromLngLat( -48.2378122,-7.2021342)
                //val firstWaypoint = Point.fromLngLat(  -48.2247439, -7.1920137 )
                //val secondWayPointPoint = Point.fromLngLat( -48.2175363 , -7.1941104 )
               // val thirdPoint = Point.fromLngLat( -48.2241323, -7.3270144 )
                //val destinationPoint = Point.fromLngLat( -48.2027166, -7.2010835 )
                val destinationPoint = Point.fromLngLat( -59.9927141, -3.1165498 )

                val userLocation = map.locationComponent.lastKnownLocation
                val points = waypoints.toList()
                //&& points.isNotEmpty()
                if (userLocation != null ) {
                    val origin = Point.fromLngLat(
                        userLocation.longitude, userLocation.latitude
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            //val routes = getDirections(listOf(origin) + points)
                            val routes = getDirections(listOf(origin,  destinationPoint ))

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


//waypoints.isNotEmpty()
 //   asx != false
    if ( asx != false) {
        route?.let {
            StartRouteControls(route = it)
        }

       SmallFloatingActionButton(
            containerColor = MaterialTheme.colorScheme.error,
            onClick = {
            mapboxMap?.markers?.forEach {
                mapboxMap?.removeMarker(it)
            }

               /* FloatingActionButton(onClick = {
                    mapboxMap?.markers?.forEach {
                        mapboxMap?.removeMarker(it)
                    }*/

            waypoints = listOf()
            route = null

            navigationMapRoute?.updateRouteVisibilityTo(false)
            navigationMapRoute?.updateRouteArrowVisibilityTo(false)
        }) {
            //Text(text = "Clear Points")
            Icon(Icons.Filled.Delete, "Floating action button.")
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
            .fillMaxSize().background(Color.Transparent)
            .padding(8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .background(
                    Color.Transparent, shape = RoundedCornerShape(4.dp)
                ) // Your colorPrimaryDark
                .padding(8.dp)
        ) {
            // Switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Simulate", color = Color.Black
                )

                Switch(checked = simulateRoute, onCheckedChange = { simulateRoute = it })
            }

            // Button
            Button(
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 5.dp),
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
