package in.teramatrix.google.sample;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import in.teramatrix.google.GoogleServices;
import in.teramatrix.google.model.Address;
import in.teramatrix.google.model.Distance;
import in.teramatrix.google.model.Place;
import in.teramatrix.google.model.TravelMode;
import in.teramatrix.google.service.DistanceCalculator;
import in.teramatrix.google.service.FusedLocationProvider;
import in.teramatrix.google.service.Geocoder;
import in.teramatrix.google.service.Locator;
import in.teramatrix.google.service.PlacesExplorer;
import in.teramatrix.google.service.ReverseGeocoder;
import in.teramatrix.google.service.RouteDesigner;

/**
 * Lets see how to use Google Services Module
 *
 * @author Mohsin Khan
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    private GoogleServices services;

    private final String TRACKING_ID    = "UA-72960268-7";

    private final String CLIENT_ID      = "gme-teramatrixtechnologies";
    private final String CRYPTO_KEY     = "XO8V3tNa30yrEDOtQ4NjN2WoOQg=";

    private final String BROWSER_KEY    = "AIzaSyBkmDhuXJup54D2y1fdYiwwvcLxj5u0oqk";
    private final String ANDROID_KEY    = "AIzaSyCyYBTJQBRYo_qlcIV9sqhJ45MfCr4LrQQ";
    private final String SERVER_KEY     = "AIzaSyBq0oOyt8KzHBpg0whNtHHPSf-Et9HPNDk";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        services = new GoogleServices(this);

        // Obtaining an instance of map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        locatorUsage();
        //fusedLocationUsage();
        //Locator locator = new Locator(map);
        //locator.locateMe(MainActivity.this);
                /*callGeocodingAsync();
                callGeocodingSync();

                callReverseGeocodingAsync();
                callReverseGeocodingSync();

                callPlacesExplorerAsync();
                callPlacesExplorerSync();
                callPlacesExplorerDirect();

                callRouteDesignerAysnc();
                callRouteDesignerSync();
                callRouteDesignerDirect();

                callDistanceMatrixSync();
                callDistanceMatrixAsync();
                callDistanceMatrixDirect();*/
    }

    //Tested
    private void fusedLocationUsage() {
        final Locator locator = new Locator(map);
        FusedLocationProvider provider = new FusedLocationProvider()
                .setContext(this)
                .setPriority(FusedLocationProvider.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000 * 5)
                .setFastestInterval(1000 * 5)
                .setLocationListener(new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            locator.locate(location);
                        }
                    }
                })
                .start();
        //fusedLocationProvider.stop();
    }

    //Tested
    private void locatorUsage() {
        Locator locator = new Locator(map)
                .setAccuracyLayer(true)
                .setMarkerIcon(R.drawable.ic_current_location)
                .setFillColor("#3273b7ff")
                .setStrokeColor("#4487f2");
        locator.locateMe(this);
    }

    //Tested
    private void callDistanceMatrixSync() {
        try {
            ArrayList<Distance> distances = services.getDistances("Nasirabad, Rajasthan", "Ajmer, Rajasthan", "Jaipur, Rajasthan", "Jodhpur, Rajasthan");
            for (Distance distance : distances) Log.e("SYNC", distance.toString());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Tested
    private void callDistanceMatrixAsync() {
        DistanceCalculator.DistanceListener listener = new DistanceCalculator.DistanceListener() {
            @Override
            public void onRequestCompleted(String json, ArrayList<Distance> distances) {
                Log.e("DIST_JSON", json);
                for (Distance distance : distances) Log.e("ASYNC", distance.toString());
            }

            @Override
            public void onRequestFailure(Exception e) {
                Log.e("ASYnc", e.getMessage());
            }
        };
        services.executeDistanceCalculator(
                SERVER_KEY,
                "Nasirabad, Rajasthan",
                listener,
                "Ajmer, Rajasthan", "Jaipur, Rajasthan", "Jodhpur, Rajasthan");
    }

    //Tested
    private void callDistanceMatrixDirect() {
        DistanceCalculator calculator = new DistanceCalculator()
                .setOrigins("Nasirabad, Rajasthan")
                .setMode(TravelMode.MODE_DRIVING)
                .setClientId(CLIENT_ID)
                .setCryptoKey(CRYPTO_KEY)
                .setResponseListener(new DistanceCalculator.DistanceListener() {
                    @Override
                    public void onRequestCompleted(String json, ArrayList<Distance> distances) {
                        for (Distance distance : distances) Log.e("DIRECT", distance.toString());
                    }

                    @Override
                    public void onRequestFailure(Exception e) {
                        Log.e("DIRECT", e.getMessage());
                    }
                });
        calculator.execute("Ajmer, Rajasthan", "Jaipur, Rajasthan", "Jodhpur, Rajasthan");
    }

    //Tested
    private void callRouteDesignerAysnc() {
        LatLng origin = new LatLng(28.453385, 77.275485);
        LatLng destination = new LatLng(21.072553, 78.932868);
        services.executeRouteDesigner(map, origin, destination, new RouteDesigner.DesignerListener() {
            @Override
            public void onRequestCompleted(String json, final Polyline[] polylines) {

            }

            @Override
            public void onRequestFailure(Exception e) {

            }
        });
    }

    //Tested
    private void callRouteDesignerSync() {
        LatLng origin = new LatLng(28.453385, 77.275485);
        LatLng destination = new LatLng(21.072553, 78.932868);
        LatLng waypoint1 = new LatLng(26.843892, 75.773492);
        LatLng waypoint2 = new LatLng(22.688522, 75.544133);
        try {
            services.addPolyline(map, origin, destination, waypoint1, waypoint2);
        } catch (ExecutionException |InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Tested
    private void callRouteDesignerDirect() {
        RouteDesigner designer = new RouteDesigner(this, map)
                .setSensor(false)
                .setAutoZoom(true)
                .setMode(TravelMode.MODE_DRIVING)
                .setAlternatives(false)
                .setOrigin(new LatLng(26.926106, 75.792809))
                .setDestination(new LatLng(26.449743, 74.704028))
                .setBaseLayer(new PolylineOptions().width(10).color(Color.parseColor("#0000FF")).geodesic(true))
                .setUpperLayer(new PolylineOptions().width(5).color(Color.parseColor("#FF0000")).geodesic(true))
                .setResponseListener(new RouteDesigner.DesignerListener() {
                    @Override
                    public void onRequestCompleted(String json, final Polyline[] polylines) {}

                    @Override
                    public void onRequestFailure(Exception e) {}
                });

        designer.design(new LatLng(25.991247, 75.664649));
    }

    //Tested
    private void callPlacesExplorerAsync() {
        services.executePlacesExplorer(
                BROWSER_KEY,
                new LatLng(26.4498954, 74.6399163),
                new PlacesExplorer.PlaceExplorerListener() {
                    @Override
                    public void onRequestCompleted(String json, ArrayList<Place> places) {
                        for (Place place : places)  {
                            map.addMarker(new MarkerOptions()
                                    .position(place.getLocation())
                                    .title(place.getName()));
                        }
                    }

                    @Override
                    public void onRequestFailure(Exception e) {

                    }
                },
                "bank", "atm");
    }

    //Tested
    private void callPlacesExplorerDirect() {
        PlacesExplorer explorer = new PlacesExplorer()
                .setKey(BROWSER_KEY)
                .setLocation(new LatLng(26.4498954, 74.6399163))
                .setResponseListener(new PlacesExplorer.PlaceExplorerListener() {
                    @Override
                    public void onRequestCompleted(String json, ArrayList<Place> places) {
                        for (Place place : places) Log.e("PLACE", place.toString());
                    }

                    @Override
                    public void onRequestFailure(Exception e) {

                    }
                });
        explorer.explore("mosque", "hindu_temple");
    }

    //Tested
    private void callPlacesExplorerSync() {
        try {
            ArrayList<Place> places = services.getPlaces(BROWSER_KEY,
                    new LatLng(26.4498954, 74.6399163), "hotel","restaurant","bar");
            for (Place place : places) Log.e("PLACE", place.toString());
        } catch (ExecutionException |InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Tested
    private void callReverseGeocodingSync() {
        try {
            Log.e("ReGeoSync", services.getAddress(new LatLng(26.4498954,74.6399163)).toString());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Tested
    private void callGeocodingSync() {
        try {
            Log.e("GeoSync", services.getLatLng("Jaipur, Rajasthan").toString());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Tested
    private void callGeocodingAsync() {
        services.executeGeocoder("Jaipur, Rajasthan", new Geocoder.GeocodingListener() {
            @Override
            public void onRequestCompleted(String json, LatLng latLng) {
                Log.e("GEO_JSON", json);
                Log.e("GeoAsync", latLng.toString());
            }

            @Override
            public void onRequestFailure(Exception e) {

            }
        });
    }

    //Tested
    private void callReverseGeocodingAsync() {
        services.executeReverseGeocoder(
                new LatLng(26.4498954, 74.6399163),
                new ReverseGeocoder.ReverseGeocodingListener() {
                    @Override
                    public void onRequestCompleted(String json, Address address) {
                        Log.e("REV_GEO_JSON", json);
                        Log.e("ReGeoAsync", address.toString());
                    }

                    @Override
                    public void onRequestFailure(Exception e) {

                    }
                });
    }
}