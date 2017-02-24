package in.teramatrix.google.utilities;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import in.teramatrix.utilities.service.LocationHandler;
import in.teramatrix.utilities.util.MapUtils;

import static in.teramatrix.utilities.service.LocationHandler.Filters.ACCURACY;
import static in.teramatrix.utilities.service.LocationHandler.Filters.DISTANCE;
import static in.teramatrix.utilities.service.LocationHandler.Filters.NULL;
import static in.teramatrix.utilities.service.LocationHandler.Filters.RADIUS;
import static in.teramatrix.utilities.service.LocationHandler.Filters.SIMILAR;
import static in.teramatrix.utilities.service.LocationHandler.Filters.ZERO;

/**
 * Lets see how to use Google utilities Module. At this time, Simply implementing location listener.
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

        // Obtaining an instance of map
        FragmentManager manager = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) manager.findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        this.locationHandler = new LocationHandler(this).setLocationListener(this)
                .setFilters(NULL, ZERO, SIMILAR, ACCURACY, RADIUS, DISTANCE).start();
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (marker == null) {
            marker = MapUtils.addMarker(map, latLng, R.drawable.ic_current_location);
            MapUtils.animateCamera(map, latLng, 12);
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
}