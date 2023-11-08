package com.stadiamaps.maplibrenavigationandroidsample

//import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.services.android.navigation.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.DirectionsResponse
//import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.services.android.navigation.v5.models.RouteOptions
import com.mapbox.services.android.navigation.v5.models.DirectionsCriteria
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Gets directions between using the Stadia Maps Navigation API.
 */
suspend fun getDirections(coordinates: List<Point>): List<com.mapbox.services.android.navigation.v5.models.DirectionsRoute> {
    // Most of this is boilerplate.
    // It is only used by the internals of the SDK.
    // As a Stadia Maps user, this is not actually translated into the API requests
    // but is used to describe the data that we always return for navigation users.
    val routeOptions = RouteOptions.builder()
        // TODO: Customize this based on your use case
        .profile(DirectionsCriteria.PROFILE_DRIVING)
        .coordinates(coordinates)
        .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
        .bannerInstructions(true)
        .voiceInstructions(true)
        .steps(true)
        .overview(DirectionsCriteria.OVERVIEW_FULL)
        .language("en")
        .baseUrl("required field that we can ignore")
        .user("required field that we can ignore")
        .accessToken("required field that we can ignore")
        .requestUuid("required field that we can ignore")
        .build()

    val locations = coordinates.map {
        mapOf("lat" to it.latitude(), "lon" to it.longitude(), "type" to "break")
    }

    // TODO: Pick an appropriate costing for your application if necessary.
    // The costing parameter must be a supported value such as auto, truck, pedestrian, etc.
    // See https://docs.stadiamaps.com/api-reference/ (costingModel) for available options.
    val costing = when (routeOptions.profile()) {
        DirectionsCriteria.PROFILE_DRIVING, DirectionsCriteria.PROFILE_DRIVING_TRAFFIC -> "auto"
        DirectionsCriteria.PROFILE_WALKING -> "pedestrian"
        DirectionsCriteria.PROFILE_CYCLING -> "bicycle"
        else -> "walking"
    }

    // Refer to our API reference for all options: https://docs.stadiamaps.com/api-reference/
    // This is just an example to demonstrate how to construct the request using maps and lists.
    val options = mapOf(
        "locations" to locations,
        "costing" to costing,
        "directions_options" to mapOf("units" to "miles"),
        "costing_options" to listOf(
            mapOf(
                costing to mapOf(
                    "use_tolls" to 0.2 // Example: automobiles avoid tolls. See the docs for details
                )
            )
        ),
    )

    val jsonOptions = JSONObject(options).toString(2)
    val postBody = jsonOptions.toByteArray()

    val url =
        URL("https://api.stadiamaps.com/navigate/v1?api_key=${Config.STADIA_API_KEY}")

    return withContext(Dispatchers.IO) {
        val conn = url.openConnection() as HttpsURLConnection
        conn.requestMethod = "POST"

        conn.doOutput = true

        conn.outputStream.use {
            it.write(postBody)
        }

        val responseString = conn.inputStream.bufferedReader().use {
            it.readText()
        }

        conn.disconnect()

        val routes =  com.mapbox.services.android.navigation.v5.models.DirectionsResponse.fromJson(responseString).routes()
        routes.map {
            // Rebuild the routes to include the options, since otherwise the SDK will crash.
            // We have to use this positively awful builder boilerplate because the interface
            // from Mapbox is incredibly hostile to bringing your own routes.
            it.toBuilder()
                .routeOptions(routeOptions)
                .build()
        }
    }
}
