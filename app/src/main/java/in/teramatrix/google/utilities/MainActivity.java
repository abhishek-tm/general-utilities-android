package in.teramatrix.google.utilities;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import in.teramatrix.utilities.service.LocationHandler;
import in.teramatrix.utilities.util.MapUtils;

/**
 * Lets see how to use utilities module to get the best known location
 * by implementing {@link LocationListener}.
 *
 * @author Mohsin Khan
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap map;
    private Marker marker;
    private LocationHandler locationHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtaining an instance of google map
        FragmentManager manager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) manager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initiating location handler with custom settings
        this.locationHandler = new LocationHandler(this).setLocationListener(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.locationHandler.start();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (marker == null) {
            marker = MapUtils.addMarker(map, latLng, R.drawable.ic_current_location);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14), 500, null);
        } else {
            marker.setPosition(latLng);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationHandler != null) {
            locationHandler.stop();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Forcing user to turn on GPS setting, It is just for demo purpose
        if (requestCode == LocationHandler.REQUEST_LOCATION) {
            locationHandler.start();
        }
    }
}