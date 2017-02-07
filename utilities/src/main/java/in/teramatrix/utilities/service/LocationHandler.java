package in.teramatrix.utilities.service;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.util.Timer;
import java.util.TimerTask;

/**
 * At the time, <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi#top_of_page">
 * Fused Location Provider Api</a> is the best way to get current location update. This class is a simple illustration of this API.
 * The main entry point for interacting with the fused location provider is {@link GoogleApiClient}.
 *
 * @author Mohsin Khan
 * @date 15/4/2016
 */
@SuppressWarnings("unused")
public class LocationHandler implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<LocationSettingsResult>, LocationListener {
    /**
     * To initialize {@link GoogleApiClient}
     */
    private Context context;

    /**
     * For a quick access to the last known / most recent location, and for the filters and comparison with
     * latest / current location.
     */
    @SuppressWarnings("WeakerAccess")
    public static Location lastLocation;

    /**
     * <b>The main entry point for Google Play services integration.</b>
     * GoogleApiClient is used with a variety of static methods. Some of these methods require that GoogleApiClient be connected,
     * some will queue up calls before GoogleApiClient is connected; Here we need to be connected to use
     * <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi">Fused Location API.</a>
     */
    private static GoogleApiClient mGoogleApiClient;

    /**
     * A data object that contains quality of service parameters for requests to the
     * <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi">Fused Location API.</a>
     * {@link LocationRequest} objects are used to request a quality of service for location updates from the
     * <a href="https://developers.google.com/android/reference/com/google/android/gms/location/FusedLocationProviderApi">Fused Location API.</a>
     */
    private static LocationRequest mLocationRequest;

    /**
     * To relieve continuous location updates
     */
    private LocationListener mLocationListener;

    /**
     * Set the priority of the request.
     * Use with a priority constant such as PRIORITY_HIGH_ACCURACY. No other values are accepted.
     * The priority of the request is a strong hint to the LocationClient for which location sources to use.
     * For example, PRIORITY_HIGH_ACCURACY is more likely to use GPS, and PRIORITY_BALANCED_POWER_ACCURACY is more likely to use
     * WIFI & Cell tower positioning, but it also depends on many other factors (such as which sources are available) and
     * is implementation dependent.
     */
    private int priority;

    /**
     * Set the desired interval for active location updates, in milliseconds.
     * The location client will actively try to obtain location updates for your application at this interval,
     * so it has a direct influence on the amount of power used by your application. Choose your interval wisely.
     */
    private long interval;

    /**
     * Explicitly set the fastest interval for location updates, in milliseconds.
     * This controls the fastest rate at which your application will receive location updates, which might be faster than setInterval(long)
     * in some situations (for example, if other applications are triggering location updates).
     * This allows your application to passively acquire locations at a rate faster than it actively acquires locations, saving power.
     */
    private long fastestInterval;

    /**
     * A set of location filter to refine location and to deliver best location.
     */
    private Filters[] filters;

    /**
     * A flag to request location services
     */
    public static final int REQUEST_LOCATION = 98;

    /**
     * Used with setPriority(int) to request the most accurate locations available.
     * This will return the finest location available.
     */
    //public static final int PRIORITY_HIGH_ACCURACY = 100;

    /**
     * Used with setPriority(int) to request "block" level accuracy.
     * Block level accuracy is considered to be about 100 meter accuracy. Using a coarse accuracy such as this often consumes less power.
     */
    //public static final int PRIORITY_BALANCED_POWER_ACCURACY = 102;

    /**
     * Used with setPriority(int) to request "city" level accuracy.
     * City level accuracy is considered to be about 10km accuracy. Using a coarse accuracy such as this often consumes less power.
     */
    //public static final int PRIORITY_LOW_POWER = 104;

    /**
     * Used with setPriority(int) to request the best accuracy possible with zero additional power consumption.
     * No locations will be returned unless a different client has requested location updates in which case this request will act as a passive listener to those locations.
     */
    //public static final int PRIORITY_NO_POWER = 105;

    public LocationHandler(Context context) {
        this.context = context;
        //Default Settings
        this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        this.interval = 1000 * 5;
        this.fastestInterval = 1000 * 5;
    }

    public LocationHandler(Context context, int priority, long interval, long fastestInterval) {
        this.context = context;
        this.priority = priority;
        this.interval = interval;
        this.fastestInterval = fastestInterval;
    }

    public LocationHandler setContext(Context context) {
        this.context = context;
        return this;
    }

    public LocationHandler setLocationListener(LocationListener mLocationListener) {
        this.mLocationListener = mLocationListener;
        return this;
    }

    public LocationHandler setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    public LocationHandler setInterval(long interval) {
        this.interval = interval;
        return this;
    }

    public LocationHandler setFastestInterval(long fastestInterval) {
        this.fastestInterval = fastestInterval;
        return this;
    }

    public LocationHandler setFilters(Filters... filters) {
        this.filters = filters;
        return this;
    }

    /**
     * To start location service,  simply call this method. In this method, a {@link NullPointerException} will
     * be thrown if context or location listener is null. Because can not move ahead without these two objects.
     *
     * In this method, {@link LocationRequest} and {@link GoogleApiClient} has been initialized with the setting provided.
     * If no configuration provided then it will be loaded with default settings.
     * @return current instance of class due to builder patter
     */
    public LocationHandler start() {
        if (context == null)
            throw new NullPointerException("Context can not be null");

        if (mLocationListener == null)
            throw new NullPointerException("LocationListener can not be null");

        mLocationRequest = new LocationRequest()
                .setInterval(interval)
                .setFastestInterval(fastestInterval)
                .setPriority(priority);

        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        return this;
    }

    /**
     * In this method, location request will be removed and {@link GoogleApiClient} will be disconnected if it is not null.
     * To stop everything related to fused location api, simply call this method. Do not remove anything manually.
     *
     * @return current instance of class due to builder patter and to restart the provide without configuring again.
     */
    @SuppressWarnings("WeakerAccess")
    public LocationHandler stop() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        return this;
    }

    @Override
    public void onConnected(Bundle bundle) {
        PendingResult<LocationSettingsResult> pendingResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest).build());
        pendingResult.setResultCallback(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        stop();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        stop();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (filters == null) {
            mLocationListener.onLocationChanged(location);
            updateLastLocation(location);
        } else {
            if (has(Filters.NULL)
                    && location == null) {
                location = lastLocation;
                location.setTime(System.currentTimeMillis());
                log("Null Location");
            } else if (has(Filters.ZERO)
                    && location.getLatitude() == 0
                    || location.getLongitude() == 0) {
                location.setLatitude(lastLocation.getLatitude());
                location.setLongitude(lastLocation.getLongitude());
                log("Zero Latitude And Longitude");
            } else if (has(Filters.ACCURACY)
                    && location.getAccuracy() > 150) {
                location.setLatitude(lastLocation.getLatitude());
                location.setLongitude(lastLocation.getLongitude());
                log("Inaccurate Location");
            } else if (has(Filters.SIMILAR)
                    && lastLocation != null
                    && lastLocation.getLongitude() == location.getLongitude()
                    && lastLocation.getLatitude() == location.getLatitude()) {
                /*
                I know the following code does not mean anything, because locations are already similar.
                But in future, there may be a correction regarding to speed, accuracy, bearing or anything.
                So it is just a tracked event here.

                It may be possible that we will not store new location or update new location. So
                That kind of work can be done here
                 */
                location.setLatitude(lastLocation.getLatitude());
                location.setLongitude(lastLocation.getLongitude());
                log("Similar Location");
            } else if (has(Filters.DISTANCE)
                    && lastLocation != null) {
                // calculating distance between new and previous location
                double earthRadius = 6371; //in kilometers
                double dLat = Math.toRadians(lastLocation.getLatitude() - location.getLatitude());
                double dLng = Math.toRadians(lastLocation.getLongitude() - location.getLongitude());
                double x = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(location.getLatitude()))
                        * Math.cos(Math.toRadians(lastLocation.getLatitude())) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
                double y = 2 * Math.atan2(Math.sqrt(x), Math.sqrt(1 - x));
                double distance = earthRadius * y; // km

                // if accuracy is more than distance between previous and current location
                if (location.getAccuracy() > distance * 1000) {
                    location.setLatitude(lastLocation.getLatitude());
                    location.setLongitude(lastLocation.getLongitude());
                    log("Inside Accuracy Layer");
                }
            }

            // finally publishing the new location
            mLocationListener.onLocationChanged(location);
            updateLastLocation(location);
        }
    }

    /**
     * Last location stored in this class will be udpated by the latest location. It is in timer because
     * last location can be used on several places in the project. If we immediatly update lastLocation
     * object then on ui (map) changes will not be reflect
     * So it should be after a few seconds.
     *
     * @param location latest location
     */
    private void updateLastLocation(final Location location) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                lastLocation = location;
            }
        }, 1000);
    }

    /**
     * Method to check whether filter exists or not. In other words, it is linear searching to search in filter array
     * that array has particular filter or not.
     *
     * @param filter filter to search
     * @return true if exists otherwise false
     */
    private boolean has(Filters filter) {
        for (Filters f : filters)
            if (f.equals(filter))
                return true;
        return false;
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        final LocationSettingsStates states = locationSettingsResult.getLocationSettingsStates();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // All location settings are satisfied. The client can
                // initialize location requests here.
                if (mGoogleApiClient.isConnected())
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                //mLocationListener.onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    if (context instanceof Activity) status.startResolutionForResult((Activity) context, REQUEST_LOCATION);
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are not satisfied. However, we have no way
                // to fix the settings so we won't show the dialog.

                break;
        }
    }

    /**
     * A simple method to log and toast a particular message. It is just for debugging and it will
     * be completely removed after test
     * @param msg message to log
     */
    private void log(String msg) {
        try {
            Log.e("FusedLocation", msg);
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * It's a list of all available location filters. An array of {@link Filters} has been defined as a global variable in this class
     * to know that what filters has been demanded by the user. Each filter has its definition in the class.
     *
     * @author Mohsin Khan
     * @date 03 February 2017
     */

    public enum Filters {
        /**
         * This filter will check whether location is null or not.
         */
        NULL,
        /**
         * This filter will check whether location's latitude and longitude is zero or not.
         */
        ZERO,
        /**
         * Location has been gathered by google's fused location api, and this api ensure fix accuracy according to priority.
         * So this filter will check accuracy of location update and compare it with accuracy proposed by google.
         * <p>
         * <a href="https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest#inherited-method-summary">
         * To know more about priority and accuracy please visit this link...
         * </a>
         */
        ACCURACY,
        /**
         * To check the current location is similar to last location.
         */
        SIMILAR,
        /**
         * This filter will calculate distance between current and last location. If distance is more than accuracy, then
         * current location will be considered otherwise last location will be delivered with updated time stamp.
         */
        DISTANCE,
        /**
         * Not implemented yet
         */
        SPEED,
        /**
         * Not implemented yet
         */
        ACCELEROMETER
    }
}