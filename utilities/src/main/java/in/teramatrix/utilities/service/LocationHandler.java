package in.teramatrix.utilities.service;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

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
 * <p>
 * One more thing to be considered, Location filters used in this program (optional), will be useful for vehicle tracking. But in the case of pedestrian,
 * It may cause the little inaccuracy in location updates. So you can use {@link LocationHandler} without {@link Filters}.
 *
 * @author Mohsin Khan
 * @date 15/4/2016
 */
@SuppressWarnings({"unused", "WeakerAccess"})
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
     * Distance limit is used while filtering the current location according to distance with last location.
     * Suppose distanceLimit is 100 meters, and the distance of current and last location is less than 100 meters,
     * The current location will not be considered and last location will be delivered with updated time stamp.
     * <br/>
     * <b>Note : Set value in meters only.</b>
     */
    private int distanceLimit;

    /**
     * This parameter is also used in location filters. Over speed location will be discarded and last location will
     * be delivered with updated timestamp.
     * <p>
     * It mean, if the speed of current location is more than {@code speedLimit}, then it will not be considered.
     * <br/>
     * <b>Note : Set value in kilometer / hour</b>
     */
    private int speedLimit;

    /**
     * This parameter is also used in location filters. Location filter will check the accuracy parameter of current
     * {@link Location}, if it is more than {@code accuracyLimit}, the current location will not be considered and
     * last location will be delivered with updated time stamp.
     * <br/>
     * <b>Note : Set value in meters</b>
     */
    private int accuracyLimit;

    /**
     * A set of location filter to refine location and to deliver best location.
     */
    private Filters[] filters;

    /**
     * A flag to request location services
     */
    public static final int REQUEST_LOCATION = 98;

    /**
     * Default constructor with one single parameter that is {@link Context}. Because all the properties have been
     * setup with their default values but {@link Context} cant be initialized.
     *
     * @param context Try to provide {@link Activity}'s context rather than app's context.
     */
    public LocationHandler(Context context) {
        this.context = context;
        //Default Settings
        this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        this.interval = 1000 * 5;
        this.fastestInterval = 1000 * 5;
        this.speedLimit = 150;
        this.distanceLimit = 30;
        this.accuracyLimit = 100;
    }

    /**
     * Another constructor to initialize the {@link LocationHandler} in a single shot. But in this, User cant configure
     * location filter's properties like {@code speedLimit} and {@code distanceLimit}.
     *
     * @param context         Try to provide {@link Activity}'s context rather than app's context.
     * @param priority        Use with a priority constant such as PRIORITY_HIGH_ACCURACY. No other values are accepted.
     * @param interval        Set the desired interval for active location updates, in milliseconds.
     * @param fastestInterval Explicitly set the fastest interval for location updates, in milliseconds.
     */
    public LocationHandler(Context context, int priority, long interval, long fastestInterval) {
        this(context);
        this.priority = priority;
        this.interval = interval;
        this.fastestInterval = fastestInterval;
        this.distanceLimit = interval > 10000 ? 100 : interval > 5000 ? 60 : 30;
        this.accuracyLimit = priority == LocationRequest.PRIORITY_HIGH_ACCURACY
                || priority == LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                ? 100 : 1000;
    }

    /**
     * Always try to supply {@link Activity}'s context rather than application's context. It doesn't mean {@link LocationHandler}
     * will not work with application's context. It will work. But if you provide {@link Activity}'s context, {@link LocationHandler}
     * will automatic request user to access gps/location service at runtime.
     *
     * @param context Try to provide {@link Activity}'s context rather than app's context.
     * @return current instance of this class
     */
    public LocationHandler setContext(Context context) {
        this.context = context;
        return this;
    }

    /**
     * New {@link Location} will be delivered in {@link LocationListener}'s {@code onLocationChanged()}.
     *
     * @param mLocationListener a concrete implementation of {@link LocationListener}
     * @return current instance of this class
     */
    public LocationHandler setLocationListener(LocationListener mLocationListener) {
        this.mLocationListener = mLocationListener;
        return this;
    }

    /**
     * Set the priority of the request.
     * Use with a priority constant such as PRIORITY_HIGH_ACCURACY. No other values are accepted.
     * The priority of the request is a strong hint to the LocationClient for which location sources to use.
     * For example, PRIORITY_HIGH_ACCURACY is more likely to use GPS, and PRIORITY_BALANCED_POWER_ACCURACY is more likely to use
     * WIFI & Cell tower positioning, but it also depends on many other factors (such as which sources are available) and
     * is implementation dependent.
     *
     * @param priority according to need of {@link Location}
     * @return current instance of this class
     */
    public LocationHandler setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Set the desired interval for active location updates, in milliseconds.
     * The location client will actively try to obtain location updates for your application at this interval,
     * so it has a direct influence on the amount of power used by your application. Choose your interval wisely.
     *
     * @param interval in milliseconds
     * @return current instance of this class
     */
    public LocationHandler setInterval(long interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Explicitly set the fastest interval for location updates, in milliseconds.
     * This controls the fastest rate at which your application will receive location updates, which might be faster than setInterval(long)
     * in some situations (for example, if other applications are triggering location updates).
     * This allows your application to passively acquire locations at a rate faster than it actively acquires locations, saving power.
     *
     * @param fastestInterval in milliseconds
     * @return current instance of this class
     */
    public LocationHandler setFastestInterval(long fastestInterval) {
        this.fastestInterval = fastestInterval;
        return this;
    }

    /**
     * Set a group of location filters. These are some checks implemented to avoid zigzag of poly-lines on map.
     *
     * @param filters an array of type {@link Filters} separated by comma
     * @return current instance of this class
     */
    public LocationHandler setFilters(Filters... filters) {
        this.filters = filters;
        return this;
    }

    /**
     * Distance limit is used while filtering the current location according to distance with last location.
     * Suppose distanceLimit is 100 meters, and the distance of current and last location is less than 100 meters,
     * The current location will not be considered and last location will be delivered with updated time stamp.
     *
     * @param distanceLimit in meters only
     * @return current instance of this class
     */
    public LocationHandler setDistanceLimit(int distanceLimit) {
        this.distanceLimit = distanceLimit;
        return this;
    }

    /**
     * This parameter is also used in location filters. Over speed location will be discarded and last location will
     * be delivered with updated timestamp. It mean, if the speed of current location is more than {@code speedLimit},
     * then it will not be considered.
     *
     * @param speedLimit in kilometer / hour
     * @return current instance of this class
     */
    public LocationHandler setSpeedLimit(int speedLimit) {
        this.speedLimit = speedLimit;
        return this;
    }

    /**
     * This parameter is also used in location filters. Location filter will check the accuracy parameter of current
     * {@link Location}, if it is more than {@code accuracyLimit}, the current location will not be considered and
     * last location will be delivered with updated time stamp.
     *
     * @param accuracyLimit in meters
     * @return current instance of this class
     */
    public LocationHandler setAccuracyLimit(int accuracyLimit) {
        this.accuracyLimit = accuracyLimit;
        return this;
    }

    /**
     * To start location service,  simply call this method. In this method, a {@link NullPointerException} will
     * be thrown if context or location listener is null. Because can not move ahead without these two objects.
     * <p>
     * In this method, {@link LocationRequest} and {@link GoogleApiClient} has been initialized with the setting provided.
     * If no configuration provided then it will be loaded with default settings.
     *
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
    public LocationHandler stop() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected())
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
        if (filters != null) {
            if (has(Filters.NULL)
                    && location == null
                    && lastLocation != null) {
                location = lastLocation;
                location.setTime(System.currentTimeMillis());
                log(location.getLatitude() + "," + location.getLongitude() + " delivered due to null location");
            } else if (has(Filters.ZERO)
                    && location != null
                    && lastLocation != null
                    && (location.getLatitude() == 0
                    || location.getLongitude() == 0)) {
                location.setLatitude(lastLocation.getLatitude());
                location.setLongitude(lastLocation.getLongitude());
                log(location.getLatitude() + "," + location.getLongitude() + " delivered due to zero latitude and longitude");
            } else if (has(Filters.ACCURACY)
                    && location != null
                    && lastLocation != null
                    && location.getAccuracy() > accuracyLimit) {
                location.setLatitude(lastLocation.getLatitude());
                location.setLongitude(lastLocation.getLongitude());
                log(location.getLatitude() + "," + location.getLongitude() + " delivered due to inaccurate location");
            } else if (has(Filters.SPEED)
                    && location != null
                    && lastLocation != null
                    && (location.getSpeed() * 3.6) > speedLimit) {
                location.setLatitude(lastLocation.getLatitude());
                location.setLongitude(lastLocation.getLongitude());
                log(location.getLatitude() + "," + location.getLongitude() + " delivered due to over speed location");
            } else if (has(Filters.RADIUS)
                    && location != null
                    && lastLocation != null
                    && location.getAccuracy() > location.distanceTo(lastLocation)) {
                // if accuracy is more than distance between previous and current location
                location.setLatitude(lastLocation.getLatitude());
                location.setLongitude(lastLocation.getLongitude());
                log(location.getLatitude() + "," + location.getLongitude() + " delivered because it's inside accuracy radius");
            } else if (has(Filters.DISTANCE)
                    && location != null
                    && lastLocation != null
                    && location.distanceTo(lastLocation) < distanceLimit) {
                // if distance between last location and current location is less than distanceLimit meters
                location.setLatitude(lastLocation.getLatitude());
                location.setLongitude(lastLocation.getLongitude());
                log(location.getLatitude() + "," + location.getLongitude() + " delivered due to very short distance");
            }
        } else {
            log(location.getLatitude() + "," + location.getLongitude() + " delivered without any filter");
        }

        // finally publishing the new location
        mLocationListener.onLocationChanged(location);
        updateLastLocation(location);
    }

    /**
     * Last location stored in this class will be updated by the latest location. It is in timer because
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
        }, 3000);
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
                    if (context instanceof Activity)
                        status.startResolutionForResult((Activity) context, REQUEST_LOCATION);
                    else if (mGoogleApiClient.isConnected())
                        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

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
     *
     * @param msg message to log
     */
    private void log(String msg) {
        try {
            Log.d(getClass().getSimpleName(), msg);
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
         * This filter will check whether location is null or not. If it is null and last stored location is not null,
         * then last location will be copied to current location.
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
         * This filter will calculate distance between current and last location. If distance is more than accuracy radius, then
         * current location will be considered otherwise last location will be delivered with updated time stamp.
         */
        RADIUS,
        /**
         * New location will only be delivered if the distance from last location is more than {@code distanceLimit} meters.
         */
        DISTANCE,
        /**
         * {@link Location} object always carry a speed value of device. It can be considered that average category vehicle
         * will run below {@code speedLimit}. This filter will discard over speed location.
         */
        SPEED,
        /**
         * Not implemented yet
         */
        ACCELEROMETER
    }
}