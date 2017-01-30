package in.teramatrix.google;

import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import in.teramatrix.google.model.Address;
import in.teramatrix.google.model.Distance;
import in.teramatrix.google.model.Place;
import in.teramatrix.google.service.DistanceCalculator;
import in.teramatrix.google.service.Geocoder;
import in.teramatrix.google.service.PlacesExplorer;
import in.teramatrix.google.service.ReverseGeocoder;
import in.teramatrix.google.service.RouteDesigner;

/**
 * Main Class of the module that will be capable to call all the services from one place. Even Those services can be also called individually,
 * but this class will provide more encapsulated methods. Each method of this class contains its documentation comments please refer them to know more.
 * Although {@link in.teramatrix.google.service.GoogleAnalyst}, {@link in.teramatrix.google.service.Locator},
 * {@link in.teramatrix.google.service.FusedLocationProvider} like services are not used in this class, you have to use them directly without
 * an instance of this class.
 *
 * @author Mohsin Khan
 * @date 3/18/2016
 */
public class GoogleServices {
    /**
     * At this version of the library, {@link Context} is used to initialize {@link GoogleMap} and
     * draw {@link Polyline} on {@link GoogleMap}. Besides it there's no use of context in other APIs (Services)
     * @since version 1.0
     */
    private Context context;

    /**
     * A default parameterize constructor to initialize {@link Context} instance.
     * @param context to initialize {@link GoogleMap} and to draw {@link Polyline}
     */
    public GoogleServices(Context context) {
        this.context = context;
    }

    /**
     * This method will simply return an instance of {@link GoogleMap}. Primarily It will create an object of {@link MapView}
     * then call some necessary methods and at last return the {@link GoogleMap}.
     * @param mapView instance of {@link MapView} to convert it into {@link GoogleMap}
     * @param savedInstanceState to be passed in {@code onCreate()} of {@link MapView}
     * @return instance of {@link GoogleMap}
     */
    @Deprecated
    public GoogleMap getMap(MapView mapView, Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        MapsInitializer.initialize(context);
        //return mapView.getMap();
        return null;
    }

    /**
     * Reverse Geo Coding means this will return address line using {@link LatLng} by calling Google's Reverse Geocoding Api <b>synchronously.</b>
     * @param latLng to be reverse geo coded
     * @return address line
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Address getAddress(LatLng latLng) throws ExecutionException, InterruptedException {
        return new ReverseGeocoder().execute(latLng).get();
    }

    /**
     * This will return {@link LatLng} of a particular address by calling Google's Geocoding api <b>synchronously.</b>
     * @param address to be geocoded
     * @return instance of {@link LatLng}
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public LatLng getLatLng(String address) throws ExecutionException, InterruptedException {
        return new Geocoder().execute(address).get();
    }

    /**
     * Method will Geo code the address in <b>asynchronous</b> manner. Here you have to provide a Callback listener that is implemented in
     * {@link Geocoder} class as {@link Geocoder.GeocodingListener}.
     * @param address to be geo coded
     * @param listener an implemented version of {@link Geocoder.GeocodingListener} interface
     */
    public void executeGeocoder(String address, Geocoder.GeocodingListener listener) {
        Geocoder geocoder = new Geocoder();
        geocoder.setResponseListener(listener);
        geocoder.execute(address);
    }

    /**
     * Method will Reverse Geo code the {@link LatLng} in <b>asynchronous</b> manner. Here you have to provide a Callback listener that is implemented in
     * {@link ReverseGeocoder} class as {@link in.teramatrix.google.service.ReverseGeocoder.ReverseGeocodingListener}.
     * @param latLng to be reverse geo coded
     * @param listener an implemented version of {@link in.teramatrix.google.service.ReverseGeocoder.ReverseGeocodingListener} interface
     */
    public void executeReverseGeocoder(LatLng latLng, ReverseGeocoder.ReverseGeocodingListener listener) {
        ReverseGeocoder reverseGeocoder = new ReverseGeocoder();
        reverseGeocoder.setResponseListener(listener);
        reverseGeocoder.execute(latLng);
    }

    /**
     * To call Google's Places API <b>synchronously</b>, this method method is designed. Method will show how to use {@link PlacesExplorer}
     * @param key Your application's API key. This key identifies your application for purposes of quota management and
     *  so that places added from your application are made immediately available to your app.
     * @param location The latitude/longitude around which to retrieve place information. This must be specified as latitude,longitude.
     * @param places Restricts the results to places matching at least one of the specified types. like "hotel", "bar", "restaurant"
     *
     * @return an {@link ArrayList} of type {@link Place}
     *
     * @throws ExecutionException
     * @throws InterruptedException
     *
     * @see Place
     */
    public ArrayList<Place> getPlaces(String key, LatLng location, String ... places) throws ExecutionException, InterruptedException {
        return new PlacesExplorer()
                .setKey(key)
                .setLocation(location)
                .execute(places).get();
    }

    /**
     * To call Google's Places API <b>asynchronously</b>, this method method is designed. Method will show how to use {@link PlacesExplorer}
     * @param key Your application's API key. This key identifies your application for purposes of quota management and
     *  so that places added from your application are made immediately available to your app.
     * @param location The latitude/longitude around which to retrieve place information. This must be specified as latitude,longitude.
     * @param listener an implemented version of {@link in.teramatrix.google.service.PlacesExplorer.PlaceExplorerListener}
     * @param places Restricts the results to places matching at least one of the specified types. like "hotel", "bar", "restaurant"
     */
    public void executePlacesExplorer(String key, LatLng location, PlacesExplorer.PlaceExplorerListener listener, String... places) {
        PlacesExplorer explorer = new PlacesExplorer()
                .setKey(key)
                .setLocation(location)
                .setResponseListener(listener);
        explorer.explore(places);
    }

    /**
     * Method is showing how to use Google's Direction Api <b>synchronously.</b>
     * @param map route will be drawn right here on this instance of GoogleMap.
     * @param origin Starting point from where route will be drawn on the map.
     * @param destination End point till where route will be drawn on the map.
     * @param waypoints Specifies an array of waypoints. Waypoints alter a route by routing it through the specified location(s).
     *
     * @return an array of {@link Polyline} which are drawn on the map.
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public Polyline [] addPolyline(GoogleMap map, LatLng origin, LatLng destination, LatLng ... waypoints) throws ExecutionException, InterruptedException {
        return new RouteDesigner(context, map)
                .setOrigin(origin)
                .setDestination(destination)
                .execute(waypoints)
                .get();
    }

    /**
     * Method is showing how to use Google's Direction Api <b>asynchronously.</b>
     * @param map route will be drawn right here on this instance of GoogleMap.
     * @param origin Starting point from where route will be drawn on the map.
     * @param destination End point till where route will be drawn on the map.
     * @param listener an implemented version of {@link in.teramatrix.google.service.RouteDesigner.DesignerListener}
     * @param waypoints Specifies an array of waypoints. Waypoints alter a route by routing it through the specified location(s).
     */
    public void executeRouteDesigner(GoogleMap map, LatLng origin, LatLng destination, RouteDesigner.DesignerListener listener, LatLng ... waypoints) {
        RouteDesigner designer = new RouteDesigner(context, map)
                .setOrigin(origin)
                .setDestination(destination)
                .setResponseListener(listener);
        designer.design(waypoints);
    }

    /**
     * A method to describe the use of {@link DistanceCalculator} class. It will <b>synchronously</b> call the Distance Matrix API and bind results
     * to {@link ArrayList} of {@link Distance}.
     * @param origin One or more locations to use as the starting point for calculating travel distance and time. But this class only supports
     *  a single origin right now. It's further implementations may expand the origins.
     * @param destinations One or more locations to use as the finishing point for calculating travel distance and time.
     *
     * @return {@link ArrayList} of {@link Distance}
     *
     * @throws ExecutionException
     * @throws InterruptedException
     *
     * @see Distance
     */
    public ArrayList<Distance> getDistances(String origin, String ... destinations) throws ExecutionException, InterruptedException {
        return new DistanceCalculator()
                .setOrigins(origin)
                .execute(destinations)
                .get();
    }

    /**
     * This method will call Google's Direction Matrix API and results will be thrown to the listener after JSON parsing.
     * @param key APIs in each platform require a specific type of key. The Google Maps Distance Matrix API will only work with a Server key.
     * APIs of the same platform can use the same key. It is for free users.
     * @param origin One or more locations to use as the starting point for calculating travel distance and time. But this class only supports
     *  a single origin right now. It's further implementations may expand the origins.
     * @param listener an implemented version of {@link in.teramatrix.google.service.DistanceCalculator.DistanceListener}
     * @param destinations One or more locations to use as the finishing point for calculating travel distance and time.
     */
    public void executeDistanceCalculator(String key, String origin, DistanceCalculator.DistanceListener listener, String ... destinations) {
        DistanceCalculator calculator = new DistanceCalculator()
                .setOrigins(origin)
                .setServerKey(key)
                .setResponseListener(listener);
        calculator.calculate(destinations);
    }
}
