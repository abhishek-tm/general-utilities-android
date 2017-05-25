package in.teramatrix.utilities.service;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import in.teramatrix.utilities.ResponseListener;
import in.teramatrix.utilities.exception.CorruptedResponseException;
import in.teramatrix.utilities.model.TravelMode;
import in.teramatrix.utilities.util.MapUtils;
import in.teramatrix.utilities.util.UrlManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static in.teramatrix.utilities.exception.CorruptedResponseException.NULL_RESPONSE;
import static in.teramatrix.utilities.exception.CorruptedResponseException.STATUS_NOT_OK;

/**
 * The Google Maps Directions API is a service that calculates directions between locations using an HTTP request. This class is written
 * to design routes on the Google Map. It is easy to use class having Builder Pattern. You can search for directions for several modes of
 * transportation, including transit, driving, walking or cycling. Directions may specify origins, destinations and waypoints either as
 * text strings (e.g. "Chicago, IL" or "Darwin, NT, Australia") or as latitude/longitude coordinates. The Directions API can return multi-part
 * directions using a series of waypoints. <strong>But this class can only process {@link LatLng} so please don't pass origin/desination or
 * waypoints as string.</strong>
 *
 * @author Mohsin Khan
 * @date 1/5/2016
 */
@SuppressWarnings("unused")
public class RouteDesigner extends AsyncTask<LatLng, Void, Polyline[]> {

    /**
     * Server response without parsing
     */
    private String json;

    /**
     * Getting map instance here because after successful response of this API,
     * route will be drawn here on this instance of GoogleMap.
     */
    private GoogleMap map;

    /**
     * This class will draw route here, that's why context is needed here to run the statements on UI thread.
     */
    private Context context;

    /**
     * Starting point from where route will be drawn on the map.
     */
    private LatLng origin;

    /**
     * End point till where route will be drawn on the map.
     */
    private LatLng destination;

    /**
     * When you calculate directions, you may specify the transportation mode to use.
     * By default, directions are calculated as driving directions.
     */
    private String mode;

    /**
     * The Google Maps API previously required that you include the sensor parameter to indicate
     * whether your application used a sensor to determine the user's location.
     * This parameter is no longer required.
     */
    private boolean sensor;

    /**
     *  If set to true, specifies that the Directions service may provide more than one route alternative in the response.
     *  Note that providing route alternatives may increase the response time from the server.
     */
    private boolean alternatives;

    /**
     * There are two polylines on the map to make emboss effect, It is one of them.
     * This {@link Polyline} will be in bottom.
     */
    private PolylineOptions baseLayer;

    /**
     * There are two polylines on the map to make emboss effect, It is one of them.
     * This {@link Polyline} will be on top of bottom layer.
     */
    private PolylineOptions upperLayer;

    /**
     * If this bit will be true then Map camera will be zoom over there automatically.
     */
    private boolean autoZoom;

    /**
     * This will publish results to your app module. Just pass an implemented version of this listener in this class
     * and results will be in your hand.
     */
    private DesignerListener listener;

    /**
     * Okhttp is a third party library to interact with the server. OkHttpClient is a factory for calls,
     * which can be used to send HTTP requests and read their responses.
     */
    private final OkHttpClient client = new OkHttpClient();

    public RouteDesigner(Context context, GoogleMap map) {
        this.context = context;
        this.map = map;
        this.json = "";

        //Default Settings
        this.mode = TravelMode.MODE_DRIVING;
        this.sensor = false;
        this.alternatives = false;
        this.autoZoom = false;
        this.baseLayer = new PolylineOptions().width(10).color(Color.parseColor("#1c83bf")).geodesic(true);
        this.upperLayer = new PolylineOptions().width(5).color(Color.parseColor("#0bb4fa")).geodesic(true);
    }

    public RouteDesigner setMap(GoogleMap map) {
        this.map = map;
        return this;
    }

    public RouteDesigner setContext(Context context) {
        this.context = context;
        return this;
    }

    public RouteDesigner setOrigin(LatLng origin) {
        this.origin = origin;
        return this;
    }

    public RouteDesigner setDestination(LatLng destination) {
        this.destination = destination;
        return this;
    }

    public RouteDesigner setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public RouteDesigner setSensor(boolean sensor) {
        this.sensor = sensor;
        return this;
    }

    public RouteDesigner setAlternatives(boolean alternatives) {
        this.alternatives = alternatives;
        return this;
    }

    public RouteDesigner setBaseLayer(PolylineOptions baseLayer) {
        this.baseLayer = baseLayer;
        return this;
    }

    public RouteDesigner setUpperLayer(PolylineOptions upperLayer) {
        this.upperLayer = upperLayer;
        return this;
    }

    public RouteDesigner setAutoZoom(boolean autoZoom) {
        this.autoZoom = autoZoom;
        return this;
    }

    public RouteDesigner setResponseListener(DesignerListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * It is just like build method of Builder Pattern. It will execute this {@link AsyncTask}
     * @param waypoints an array of all the waypoints but should be less then 10
     */
    public void design(LatLng ... waypoints) {
        //If Google Map is null
        if (map == null)
            throw new NullPointerException("Google Map can not be null");

        //If context is null
        if (context == null)
            throw new NullPointerException("Context can not be null");

        //If origin or destination is null
        if (origin == null || destination == null)
            throw new NullPointerException("Origin or Destination can not be null");

        this.execute(waypoints);
    }

    @Override
    protected Polyline [] doInBackground(LatLng... waypoints) {
        final Polyline polylines [] = new Polyline[2];
        try {
            //Building request and making call
            Request request = new Request.Builder().url(UrlManager.getDirectionApiUrl
                    (origin, destination, sensor, mode, alternatives, waypoints)).build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                //Parsing JSON response
                json = response.body().string();
                JSONObject object = new JSONObject(json);
                if (object.getString("status").equalsIgnoreCase("OK")) {
                    JSONArray routeArray = object.getJSONArray("routes");
                    JSONObject routes = routeArray.getJSONObject(0);
                    JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                    String encodedString = overviewPolylines.getString("points");
                    final List<LatLng> list = decodePoly(encodedString);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int z = 0; z < list.size() - 1; z++) {
                                baseLayer.add(list.get(z), list.get(z + 1));
                                upperLayer.add(list.get(z), list.get(z + 1));
                            }
                            polylines[0] = map.addPolyline(baseLayer);
                            polylines[1] = map.addPolyline(upperLayer);
                        }
                    });
                } else {
                    //If Google's API status is not ok
                    throw new CorruptedResponseException(STATUS_NOT_OK);
                }
            } else {
                //If response is not successful
                throw new CorruptedResponseException(NULL_RESPONSE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onRequestFailure(e);
                listener = null;
            }
        }
        return polylines;
    }

    @Override
    protected void onPostExecute(Polyline [] polylines) {
        super.onPostExecute(polylines);

        if (autoZoom && polylines != null && polylines[0] != null) {
            List<LatLng> list = polylines[0].getPoints();
            if (list.size() > 0)
            MapUtils.animateCameraToGroup(map, list.toArray(new LatLng[list.size()]));
        }

        if (listener != null) listener.onRequestCompleted(json, polylines);
    }

    /**
     * This code will perform calculations on the json and decode the data for Polylines
     * This decoding algorithm is provided by the vendor and 100% tested. So we will keep it untouched.
     */
    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)), (((double) lng / 1E5) ));
            poly.add(p);
        }
        return poly;
    }

    /**
     * An interface to publish results in the caller classes. By implementing this,
     * end user of the module can access final results.
     */
    public interface DesignerListener extends ResponseListener {

        /**
         * This method will be invoked on a successful http request.
         * @param json
         * It is the response received from the server after request.
         * @param polylines
         * These are {@link Polyline} drawn on the map. These are removable from the map
         */
        void onRequestCompleted(String json, Polyline[] polylines);
    }
}