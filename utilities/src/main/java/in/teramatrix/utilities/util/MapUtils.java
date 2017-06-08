package in.teramatrix.utilities.util;

import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

/**
 * Class has been constructed to collect some common utilities of Google Maps for example moving or animating
 * map camera to particular location or on a group of locations, plotting marker, removing polylines etc.
 * @author Mohsin Khan
 * @date 3/21/2016
 */
@SuppressWarnings("unused")
public class MapUtils {
    /**
     * Method will move camera to the group of LatLng objects with animation
     * @param map on which the camera will be animated
     * @param padding from the border of map
     * @param latLngs comma separated LatLng objects that are to be included in group
     */
    public static void animateCameraToGroup(GoogleMap map, int padding, LatLng... latLngs) {
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng l : latLngs) builder.include(l);
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
    }

    /**
     * Method will move camera to the group of LatLng objects with animation having default padding of 100
     * @param map on which the camera will be animated
     * @param latLngs comma separated LatLng objects that are to be included in group
     */
    public static void animateCameraToGroup(GoogleMap map, LatLng... latLngs) {
        if (map != null) {
            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng l : latLngs) builder.include(l);
            try {
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method will move camera to the group of LatLng objects without animation
     * @param padding from the border of map
     * @param latLngs comma separated LatLng objects that are to be included in group
     */
    public static void moveCameraToGroup(GoogleMap map, int padding, LatLng... latLngs) {
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng l : latLngs) {
            builder.include(l);
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), padding));
    }

    /**
     * Method will move camera to the group of LatLng objects without animation having default padding of 100
     * @param latLngs comma separated LatLng objects that are to be included in group
     */
    public static void moveCameraToGroup(GoogleMap map, LatLng... latLngs) {
        final LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng l : latLngs) {
            builder.include(l);
        }
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
    }

    /**
     * Whenever user clicks on any marker on the map, an info window is visible. If there's a requirement to change the layout of
     * default info window then this method may be helpful. In the method, {@code getInfoContents()} has been overridden to set up custom
     * info window.
     * @param map on which the info window has to be set
     * @param view custom layout
     */
    public static void setInfoWindow(GoogleMap map, final View view) {
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return view;
            }
        });
    }

    /**
     * It's a small animation code to bounce selected marker on the map, To make animation,
     * Bounce Interpolator has been used.
     *
     * @param marker which is to be bounced
     */
    public static void dropPinEffect(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1000;
        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post again 15ms later.
                    handler.postDelayed(this, 15);
                } else {
                    marker.showInfoWindow();
                }
            }
        });
    }
}