package in.teramatrix.utilities.service;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Timer;
import java.util.TimerTask;

import in.teramatrix.utilities.R;
import in.teramatrix.utilities.util.CoordinateUtilities;
import in.teramatrix.utilities.util.MapUtils;

/**
 * This class is designed to show current location on {@link GoogleMap}. Here A {@link Marker} and a {@link Circle} will be plotted on the map.
 * Marker will show the position and circle will show accuracy of location. To animate circle according to increment and decrement in location
 * accuracy, a method is defined that is {@code animateCircle()}. There is two methods to make this class work, these are {@code locate()} and
 * {@code locateMe()}. In {@code locate()}, you can pass any location but on the other hand, in {@code lcoateMe()}, {@link LocationHandler}
 * will be used to get location and only current location will shown on the map. {@code locateMe()} will return the running instance of
 * {@link LocationHandler} so you can stop location updates anytime by calling {@code stop()} method of {@link LocationHandler} class
 * or to stop locating, just call {@code stop()} of this class. It will remove {@link Marker} and {@link Circle} also.
 * @author Mohsin Khan
 * @date 4/13/2016
 */
@SuppressWarnings("unused")
public class Locator {
    /**
     * On which the location marker will be plotted
     */
    private GoogleMap map;

    /**
     * A single instance of marker, to show the location
     */
    private Marker marker;

    /**
     * Specially used to present accuracy of location
     */
    private Circle circle;

    /**
     * If calculating distance from latest location, this will be the factor from the distance will be calculated.
     */
    private Location recent;

    /**
     * To fetch current location of device. This will only be initialized when {@code locateMe()} method is called
     */
    private LocationHandler locationHandler;

    /**
     * Accuracy layer ({@link Circle} will not be visible if this is false.
     */
    private boolean accuracyLayer;

    /**
     * Marker will be rotated according to bearing of last location if true
     */
    private boolean rotation;

    /**
     * Marker will be moved slowly to the next location if it is true
     */
    private boolean movingMarker;

    /**
     * To be filled in circle that will show accuracy
     */
    private String fillColor;

    /**
     * It is border color of the circle that will show accuracy
     */
    private String strokeColor;

    /**
     * Location marker that will be shown on the map
     */
    private int markerIcon;

    /**
     * Default constructor of the class
     * @param map on which the current location will be displayed
     */
    public Locator(GoogleMap map) {
        this.map = map;
        //Default Setting
        this.fillColor = "#3273b7ff";
        this.strokeColor = "#4487f2";
        this.accuracyLayer = true;
        this.rotation = false;
        this.movingMarker = false;
        this.markerIcon = R.drawable.ic_current_location;
    }

    public Locator setAccuracyLayer(boolean accuracyLayer) {
        this.accuracyLayer = accuracyLayer;
        return this;
    }

    public Locator setRotation(boolean rotation) {
        this.rotation = rotation;
        return this;
    }

    public Locator setFillColor(String fillColor) {
        this.fillColor = fillColor;
        return this;
    }

    public Locator setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }

    public Locator setMarkerIcon(int markerIcon) {
        this.markerIcon = markerIcon;
        return this;
    }

    public void setMovingMarker(boolean movingMarker) {
        this.movingMarker = movingMarker;
    }

    /**
     * This will plot current location {@link Marker} on the {@link GoogleMap} using {@link LocationHandler}
     * @param context to initialize {@link LocationHandler}
     * @return running instance of {@link LocationHandler} to stop updates later or whenever you want.
     */
    public LocationHandler locateMe(Context context) {
        locationHandler = new LocationHandler(context)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000 * 5)
                .setFastestInterval(1000 * 5)
                .setLocationListener(new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        locate(location);
                    }
                })
                .start();
        return locationHandler;
    }

    /**
     * This method will maintain a {@link Marker} and a {@link Circle} on the map. Every time a location is passed in this method,
     * and the method will remove previous {@link Marker} & {@link Circle} and plot a new {@link Marker} and {@link Circle} on the new {@link Location}
     * you passed in the parameter.
     * @param location where current location marker will be plotted
     */
    public Marker locate(Location location) {
        if (location != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (marker == null) {
                if (map != null) {
                    marker = MapUtils.addMarker(map, latLng, markerIcon, true);
                    if (accuracyLayer)
                    circle = map.addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(location.getAccuracy())
                            .strokeColor(Color.parseColor(strokeColor))
                            .strokeWidth(0.8f)
                            .fillColor(Color.parseColor(fillColor)));
                }
            } else {
                if (!CoordinateUtilities.isEqual(location, recent)) {
                    if (movingMarker) {
                        moveMarker(marker, latLng);
                    } else {
                        marker.setPosition(latLng);
                    }

                    if (rotation) {
                        marker.setRotation(recent.bearingTo(location));
                    }

                    if(accuracyLayer) {
                        circle.setCenter(latLng);
                        animateCircle(circle, Math.round(location.getAccuracy()));
                    }
                }
            }
            recent = location;
        }
        return marker;
    }

    /**
     * Method will generate animation like effects by increasing/decreasing the value of {@link Circle} radius using
     * Android's {@link Handler}.
     * One more thing to consider that here I'm checking if new radius is just one meter up/down with the existing
     * radius then this animation will not work. It will be stable in that condition.
     * @param c circle to be animated
     * @param r new radius value
     */
    private void animateCircle(final Circle c, final double r) {
        c.setRadius(Math.round(c.getRadius()));
        if (c.getRadius() != r && c.getRadius() != r - 1 && c.getRadius() != r + 1) {
            //Calculating difference
            final int d = (int) Math.abs(c.getRadius() - r);
            //Calculating speed of circle resizing/scaling
            final int s = (1000 + d) / d;
            final Handler h = new Handler();
            h.post(new Runnable() {
                public void run() {
                    c.setRadius((c.getRadius() < r) ? c.getRadius() + 1 : c.getRadius() - 1);
                    if (c.getRadius() == r) {
                        h.removeCallbacks(this);
                        return;
                    }
                    h.postDelayed(this, s);
                }
            });
        }
    }

    /**
     * Method will generate animation like effects by increasing/decreasing the value of {@link Circle} radius using
     * Java's {@link Timer}.
     * One more thing to consider that here I'm checking if new radius is just one meter up/down with the existing
     * radius then this animation will not work. It will be stable in that condition.
     * @param cxt context to execute on main thread
     * @param c circle to be animated
     * @param r new radius value
     */
    private void animateCircle(final Context cxt, final Circle c, final double r) {
        c.setRadius(Math.round(c.getRadius()));
        if (c.getRadius() != r && c.getRadius() != r - 1 && c.getRadius() != r + 1) {
            //Calculating difference
            final int d = (int) Math.abs(c.getRadius() - r);
            //Calculating speed of circle resizing
            final int s = (1000 + d) / d;
            final Timer t = new Timer();
            TimerTask timerTask = new TimerTask() {
                @Override
                public void run() {
                    if (cxt != null)
                        ((Activity)cxt).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                c.setRadius((c.getRadius() < r) ? c.getRadius() + 1 : c.getRadius() - 1);
                                if (c.getRadius() == r)  {
                                    t.cancel();
                                }
                            }
                        });
                }
            };
            t.schedule(timerTask, 0, 50);
        }
    }

    /**
     * @param circle circle to be animated
     */
    private void animateCircle(final Circle circle) {
        ValueAnimator vAnimator = new ValueAnimator();
        vAnimator.setRepeatCount(ValueAnimator.INFINITE);
        vAnimator.setRepeatMode(ValueAnimator.RESTART);  /* PULSE */
        vAnimator.setIntValues(0, 100);
        vAnimator.setDuration(1000);
        vAnimator.setEvaluator(new IntEvaluator());
        vAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        vAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                circle.setRadius(animatedFraction * 100);
            }
        });
        vAnimator.start();
    }

    /**
     * Method will move marker from one location to another location smoothly.
     * @param marker which is to be moved
     * @param toPosition destination
     */
    private void moveMarker(final Marker marker, final LatLng toPosition) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1000 * 5;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    /**
     * To stop monitoring current location. This method will stop Location Provider that is {@link LocationHandler}
     * and will remove current location {@link  Marker} and accuracy {@link Circle}.
     */
    public void stop() {
        if (marker != null) marker.remove();
        if (circle != null) circle.remove();
        if (locationHandler != null) locationHandler.stop();
    }
}
