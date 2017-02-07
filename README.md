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
    compile 'com.github.abhishektm:google-utilities:1.0.0'
}
```

### Geocoder

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