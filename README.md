# Google Services And Utilities
It's a pack of some general utilities useful for almost all projects. We all need Google APIs somewhere in our projects.
Google APIs is a set of application programming interfaces (APIs) developed by Google which allow communication with Google Services and their integration to other services.
Examples of these include Search, Gmail, Translate or Google Maps. Third-party apps can use these APIs to take advantage of or extend the functionality of the existing services.

In this module, we've included following APIs...

* [Geo Coding](https://developers.google.com/maps/documentation/geocoding/intro#BYB)
* [Reverse Geo Coding](https://developers.google.com/maps/documentation/geocoding/intro#ReverseGeocoding)
* [Places API](https://developers.google.com/places/)
* [Directions API](https://developers.google.com/maps/documentation/directions/start)
* [Distance Matrix API](https://developers.google.com/maps/documentation/distance-matrix/intro#Introduction)
* [Fused Location Provider Api](https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi#top_of_page)
* [Google Analytics](https://developers.google.com/analytics/devguides/collection/android/v4/#set-up-your-project)
* And some other features.

### Download .apk file

[Click here to download debug version…](app/build/outputs/apk)

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
    compile 'com.github.abhishek-tm:google-utilities:1.0.5'
}
```

### Geocoder
Geocoding is the process of converting addresses (like "1600 Amphitheatre Parkway, Mountain View, CA") into geographic coordinates (like latitude 37.423021 and longitude -122.083739),
which you can use to place markers on a map, or position the map.
Just pass your address to `execute()` method, it will publish result in `onRequestCompleted()`.

```java
Geocoder geocoder = new Geocoder();
geocoder.setResponseListener(new Geocoder.GeocodingListener() {
    @Override
    public void onRequestCompleted(String json, LatLng latLng) {
        // Returned JSON response and LatLng object
    }

    @Override
    public void onRequestFailure(Exception e) {
        // handle exception here
    }
});
geocoder.execute("New Delhi, India");
```

### Reverse Geocoder
Reverse geocoding is the process of converting geographic coordinates into a human-readable address.
Just pass `LatLng` object to `execute()` method, it will publish result in `onRequestCompleted()`.

```java
ReverseGeocoder reverseGeocoder = new ReverseGeocoder();
reverseGeocoder.setResponseListener(new ReverseGeocoder.ReverseGeocodingListener() {
    @Override
    public void onRequestCompleted(String json, Address address) {
        // Returned JSON response and Address object
    }

    @Override
    public void onRequestFailure(Exception e) {
        // handle exception here
    }
});
reverseGeocoder.execute(new LatLng(26.896079, 75.744542));
```

### Places Explorer
The Google Places API for Android provides your app with rich information about places, including the place's name and address, the geographical location specified as
latitude/longitude coordinates, the type of place (such as night club, pet store, museum), and more.
A browser key is needed to call this api, so you have to supply it. Browser key can be obtained from Google Developer Console. And put all the places type in `explore()` method.

```java
new PlacesExplorer()
    .setKey(BROWSER_KEY)
    .setLocation(new LatLng(26.4498954, 74.6399163))
    .setResponseListener(new PlacesExplorer.PlaceExplorerListener() {
        @Override
        public void onRequestCompleted(String json, ArrayList<Place> places) {
            // All available places as an array list of place objects
            for (Place place : places) Log.e("PLACE", place.toString());
        }

        @Override
        public void onRequestFailure(Exception e) {
            // handle exception here
        }
    }).explore("bank", "atm");
```

### Route Designer
To design route with polyilne on Google Map between two points, use this simple code snippet with default configuration.

```java
new RouteDesigner(this, map)
    .setOrigin(new LatLng(26.926106, 75.792809))
    .setDestination(new LatLng(26.449743, 74.704028))
    .design();
```

### Distance Calculator
The Google Maps Distance Matrix API returns information based on the recommended route between start and end points,
as calculated by the Google Maps API, and consists of rows containing duration and distance values for each pair.
Set origin using `setOrigin()` method and destination will be inserted in `execute()` method.

In this version, origin will be single and destination may be multiple.

```java
new DistanceCalculator()
    .setOrigins("Ajmer, Rajasthan")
    .setServerKey(SERVER_KEY)
    .setResponseListener(new DistanceCalculator.DistanceListener() {
        @Override
        public void onRequestCompleted(String json, ArrayList<Distance> distances) {
            for (Distance distance : distances) Log.e("DISTANCE", distance.toString());
        }

        @Override
        public void onRequestFailure(Exception e) {
            Log.e("DISTANCE", e.getMessage());
        }
    }).execute("Jaipur, Rajasthan", "Delhi", "Mumbai");
```

### Location Handler
Using Google's Fused location API, get the best known location of the device in simple steps.

```java
new LocationHandler(this)
    .setLocationListener(new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
        // Get the best known location
    }
}).start();
```

### License
Copyright (C) 2017  Teramatrix Technologies Private Limited

> This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.<br/><br/>
> This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.<br/><br/>
> You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
