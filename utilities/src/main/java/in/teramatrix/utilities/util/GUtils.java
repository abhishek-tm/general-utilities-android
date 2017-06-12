package in.teramatrix.utilities.util;

import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Date;

/**
 * Class has been constructed to collect some common utilities of Google Maps for example moving or animating
 * map camera to particular location or on a group of locations, plotting marker, removing polylines etc.
 *
 * @author Mohsin Khan
 * @date 3/21/2016
 */
@SuppressWarnings("unused")
public class GUtils {
    /**
     * Method will simply convert a {@link Location} instance to string and log it.
     *
     * @param current to be logged in logcat
     * @param recent  if want to see distance from previous location then pass last {@link Location}
     */
    public static void logDetails(Location current, Location recent) {
        if (current != null) {
            Log.e("Location", "Accuracy: " + current.getAccuracy()
                    + "|Speed: " + current.getSpeed()
                    + "|LatLng: " + current.getLatitude() + ", " + current.getLongitude()
                    + "|Time: " + new Date(current.getTime()).toString()
                    + "|Provider: " + current.getProvider().toUpperCase()
                    + ((recent != null) ? "|Distance: " + current.distanceTo(recent) : ""));
        }
    }

    /**
     * @param a first location
     * @param b second location
     * @return true if a & b have similar latitude and longitude
     */
    public static boolean isEqual(Location a, Location b) {
        return (a.getLatitude() == b.getLatitude()) && (a.getLongitude() == b.getLongitude());
    }

    /**
     * @param a first location
     * @param b second location
     * @return true if a & b have similar latitude and longitude
     */
    public static boolean isEqual(LatLng a, LatLng b) {
        if (a.latitude == b.latitude)
            if (a.longitude == b.longitude)
                return true;
        return false;
    }

    /**
     * @param a starting point
     * @param b ending point
     * @return center point from starting to ending point
     */
    public static LatLng midPoint(LatLng a, LatLng b) {
        double lat1 = Math.toRadians(a.latitude);
        double lat2 = Math.toRadians(b.latitude);
        double lon1 = Math.toRadians(a.longitude);
        double lon2 = b.longitude;
        double dLon = Math.toRadians(lon2 - a.longitude);
        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        return new LatLng(Math.toDegrees(Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) *
                (Math.cos(lat1) + Bx) + By * By))), Math.toDegrees(lon1 + Math.atan2(By, Math.cos(lat1) + Bx)));
    }

    /**
     * @param a starting point
     * @param b ending point
     * @return distance between these two points
     */
    public static double getDistance(LatLng a, LatLng b) {
        double earthRadius = 6371; //in kilometers
        double dLat = Math.toRadians(b.latitude - a.latitude);
        double dLng = Math.toRadians(b.longitude - a.longitude);
        double x = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(a.latitude))
                * Math.cos(Math.toRadians(b.latitude)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double y = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
        return earthRadius * y;
    }

    /**
     * A method to check whether the {@link LatLng} belongs to a connected graph of {@link LatLng}s (Geo-Fence)
     * or not.
     *
     * @param point location to check
     * @param list  list of geo-fence points
     * @return true if location(point) is inside the geo-fence otherwise false
     */
    public static boolean isInRegion(LatLng point, ArrayList<LatLng> list) {
        int crossings = 0;
        int count = list.size();
        // for each edge
        for (int i = 0; i < count; i++) {
            LatLng a = list.get(i);
            int j = i + 1;
            if (j >= count) {
                j = 0;
            }
            LatLng b = list.get(j);
            if (rayCrossesSegment(point, a, b)) {
                crossings++;
            }
        }
        // odd number of crossings?
        return (crossings % 2 == 1);
    }

    /**
     * A helper method of {@code isInRegion}
     */
    private static boolean rayCrossesSegment(LatLng point, LatLng a, LatLng b) {
        double px = point.longitude;
        double py = point.latitude;
        double ax = a.longitude;
        double ay = a.latitude;
        double bx = b.longitude;
        double by = b.latitude;
        if (ay > by) {
            ax = b.longitude;
            ay = b.latitude;
            bx = a.longitude;
            by = a.latitude;
        }
        // alter longitude to cater for 180 degree crossings
        if (px < 0) {
            px += 360;
        }
        ;
        if (ax < 0) {
            ax += 360;
        }
        ;
        if (bx < 0) {
            bx += 360;
        }
        ;

        if (py == ay || py == by) py += 0.00000001;
        if ((py > by || py < ay) || (px > Math.max(ax, bx))) return false;
        if (px < Math.min(ax, bx)) return true;

        double red = (ax != bx) ? ((by - ay) / (bx - ax)) : Float.MAX_VALUE;
        double blue = (ax != px) ? ((py - ay) / (px - ax)) : Float.MAX_VALUE;
        return (blue >= red);
    }

    /**
     * Method will move camera to the group of LatLng objects with animation
     *
     * @param map     on which the camera will be animated
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
     *
     * @param map     on which the camera will be animated
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
     *
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
     *
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
     *
     * @param map  on which the info window has to be set
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