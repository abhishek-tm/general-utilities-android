package in.teramatrix.google.util;

import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

/**
 * Class has been constructed to collect some common utilities of Google Maps for example moving or animating
 * map camera to particular location or on a group of locations, plotting marker, removing polylines etc.
 * @author Mohsin Khan
 * @date 3/21/2016
 */
@SuppressWarnings("unused")
public class GoogleMapUtilities {
    /**
     * This will simply remove polylines on the map
     * @param polylines which are to be removed
     */
    public static void removePolyLines(Polyline ... polylines) {
        for (Polyline polyline : polylines)
            polyline.remove();
    }

    /**
     * This will plot marker on the map
     * @param map on which marker has to be plotted
     * @param position on which position, marker has to be plotted
     * @return marker which has been added
     */
    public static Marker addMarker(GoogleMap map, LatLng position) {
        return map.addMarker(new MarkerOptions().position(position));
    }

    /**
     * This will plot marker on the map
     * @param map on which marker has to be plotted
     * @param position on which position, marker has to be plotted
     * @param title title text for info window
     * @return marker which has been added
     */
    public static Marker addMarker(GoogleMap map, LatLng position, String title) {
        return map.addMarker(new MarkerOptions()
                .title(title)
                .position(position));
    }

    /**
     * This will plot marker on the map
     * @param map on which marker has to be plotted
     * @param position on which position, marker has to be plotted
     * @param title title text for info window
     * @param icon icon id for example {@code R.drawable.ic_marker}
     * @return marker which has been added
     */
    public static Marker addMarker(GoogleMap map, LatLng position, String title, int icon) {
        return map.addMarker(new MarkerOptions()
                .title(title)
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(icon)));
    }

    /**
     * This will plot marker on the map
     * @param map on which marker has to be plotted
     * @param position on which position, marker has to be plotted
     * @param icon icon id for example {@code R.drawable.ic_marker}
     * @param isCentered it will set anchor(0.5f, 0.5f) if true
     * @return marker which has been added
     */
    public static Marker addMarker(GoogleMap map, LatLng position, int icon, boolean isCentered) {
        return map.addMarker(new MarkerOptions()
                .position(position)
                .anchor((isCentered) ? 0.5f : 0f, (isCentered) ? 0.5f : 0f)
                .icon(BitmapDescriptorFactory.fromResource(icon)));
    }

    /**
     * This will simply animate the camera on to the position
     * @param map Map
     * @param position on which position the camera will be animated/moved
     * @param zoom zoom level
     */
    public static void animateCamera(GoogleMap map, LatLng position, int zoom) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
    }

    /**
     * This will simply move the camera on to the position
     * @param map Map
     * @param position on which position the camera will be moved
     * @param zoom zoom level
     */
    public static void moveCamera(GoogleMap map, LatLng position, int zoom) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, zoom));
    }

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