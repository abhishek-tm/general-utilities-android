# Google Services And Utilities
It's a pack of some general utilities useful for almost all projects. We all need Google APIs somewhere in our projects.
Google APIs is a set of application programming interfaces (APIs) developed by Google which allow communication with Google Services and their integration to other services.
Examples of these include Search, Gmail, Translate or Google Maps. Third-party apps can use these APIs to take advantage of or extend the functionality of the existing services.

In this module, we've included following APIs...

* [Geo Coding] (https://developers.google.com/maps/documentation/geocoding/intro#BYB)
* [Reverse Geo Coding] (https://developers.google.com/maps/documentation/geocoding/intro#ReverseGeocoding)
* [Places API] (https://developers.google.com/places/place-id#example-using-the-places-api-web-service)
* [Directions API] (https://developers.google.com/maps/documentation/directions/start)
* [Distance Matrix API] (https://developers.google.com/maps/documentation/distance-matrix/intro#Introduction)
* [Fused Location Provider Api] (https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi#top_of_page)
* [Google Analytics] (https://developers.google.com/analytics/devguides/collection/android/v4/#set-up-your-project)
* And some other features.

### Importing the Library

Simply add the following repositories to your project level `build.gradle` file:

```groovy
allprojects {
    repositories {
        jcenter()

        maven {
            url = 'https://jitpack.io'
        }
    }
}
```

And add the following dependency to your app level `build.gradle` file:
```groovy
dependencies {
    compile 'com.github.khan-tm:google-utilities:1.1'
}
```