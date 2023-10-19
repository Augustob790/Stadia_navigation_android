# Sample code for MapLibre Navigation Android

This is a sample project demonstrating an extremely basic integration of
[MapLibre Navigation Android](https://github.com/maplibre/maplibre-navigation-android),
a fork of Mapbox Navigation.
The demo allows you to tap a point on the map,
get directions using the Stadia Maps Navigation API,
and launch turn-by-turn navigation with full support for banners and voice guidance.

## Who is it for?

This is intended for prospective users of the Stadia Maps navigation endpoint.
If you are interested in a trial, send an email to entsales@stadiamaps.com.

## How do I get started?

First, replace the API key placeholders in [`res/styles.xml`](app/src/main/res/values/styles.xml)
and [`Routing.kt`](app/src/main/java/com/stadiamaps/maplibrenavigationandroidsample/Config.kt)
with a real key.
Contact us for details if you are not already an enterprise customer.

The `getDirections` function is the core of the API integration.
You may want to customize a few parameters
to take advantage of our flexible [API](https://docs.stadiamaps.com/api-reference/)
(see `routeRequest` for the request model).
We have marked the areas most likely to warrant a second look with `TODO` comments.

UI customization requries a bit more effort.
There is no shortcut to experience,
but our sample code should provide a good starting point.
If you are looking to build your own interface or customize the behavior in any way,
you may need to check out using the `NavigationView` directly.
The sample code use the `NavigationLauncher` for convenience,
but you man find that you need to use other APIs to get the UI/UX you want.

In particular, note that recalculating when the user goes off route
does not work out of the box with the defaults.
Most apps will want to hook into the events and handle recalculation ad hoc.

## Support

If you have any issues with the `getDirections` code,
or anything else related to the Stadia Maps API,
we're happy to help!
Questions related to MapLibre Navigation Android can be discussed on their
[GitHub project](https://github.com/maplibre/maplibre-navigation-android).
Feel free to tag `@ianthetechie` though for greater visibility.
