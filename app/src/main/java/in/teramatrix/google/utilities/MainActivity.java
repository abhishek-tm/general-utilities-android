package in.teramatrix.google.utilities;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import in.teramatrix.utilities.model.Address;
import in.teramatrix.utilities.model.Distance;
import in.teramatrix.utilities.model.Place;
import in.teramatrix.utilities.model.TravelMode;
import in.teramatrix.utilities.service.DistanceCalculator;
import in.teramatrix.utilities.service.Geocoder;
import in.teramatrix.utilities.service.LocationHandler;
import in.teramatrix.utilities.service.Locator;
import in.teramatrix.utilities.service.PlacesExplorer;
import in.teramatrix.utilities.service.ReverseGeocoder;
import in.teramatrix.utilities.service.RouteDesigner;

/**
 * Lets see how to use Google Services Module
 *
 * @author Mohsin Khan
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;

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

        // Obtaining an instance of map
        FragmentManager manager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) manager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        locatorUsage();
    }

    //Tested
    private void fusedLocationUsage() {
        final Locator locator = new Locator(map);
        LocationHandler provider = new LocationHandler(this)
                .setPriority(LocationHandler.PRIORITY_HIGH_ACCURACY)
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
    }

    //Tested
    private void locatorUsage() {
        Locator locator = new Locator(map)
                .setMarkerIcon(R.drawable.ic_current_location)
                .setFillColor("#3273b7ff")
                .setStrokeColor("#4487f2");
        locator.locateMe(this);
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
}