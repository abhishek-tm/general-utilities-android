# Google Services And Utilities
It's a pack of some general utilities useful for almost all projects. We all need Google APIs somewhere in our projects.
Google APIs is a set of application programming interfaces (APIs) developed by Google which allow communication with Google Services and their integration to other services.
Examples of these include Search, Gmail, Translate or Google Maps. Third-party apps can use these APIs to take advantage of or extend the functionality of the existing services.

In this module, we've included following APIs...

* [Geo Coding] (https://developers.google.com/maps/documentation/geocoding/intro#BYB)
* [Reverse Geo Coding] (https://developers.google.com/maps/documentation/geocoding/intro#ReverseGeocoding)
* [Places API] (https://developers.google.com/places/)
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
    compile 'com.github.abhishektm:google-utilities:1.0.0'
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

```java
new RouteDesigner(this, map)
    .setOrigin(new LatLng(26.926106, 75.792809))
    .setDestination(new LatLng(26.449743, 74.704028))
    .design();
```

### Distance Calculator

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

### Licence

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this work except in compliance with the License.
> You may obtain a copy of the License in the LICENSE file, or at:
>
>  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.