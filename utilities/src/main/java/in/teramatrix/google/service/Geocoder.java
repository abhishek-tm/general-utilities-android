package in.teramatrix.google.service;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import in.teramatrix.google.ResponseListener;
import in.teramatrix.google.exception.CorruptedResponseException;
import in.teramatrix.google.util.UrlManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static in.teramatrix.google.exception.CorruptedResponseException.EMPTY_ARRAY;
import static in.teramatrix.google.exception.CorruptedResponseException.NULL_RESPONSE;
import static in.teramatrix.google.exception.CorruptedResponseException.STATUS_NOT_OK;

/**
 * A class for handling geocoding. Geocoding is the process of transforming a street address or other description of a location into
 * a (latitude, longitude) coordinate. To get LatLng of specified address, we are interacting to the Google Server (callling Api)
 * and broadcasting results in {@link in.teramatrix.google.service.Geocoder.GeocodingListener} after parsing JSON.
 * @author Mohsin Khan
 * @date 1/5/2016
 */
@SuppressWarnings("unused")
public class Geocoder extends AsyncTask<String, Void, LatLng> {

    /**
     * Server response without parsing
     */
    private String json;

    /**
     * This will publish results to your app module. Just pass an implemented version of this listener in this class
     * and results will be in your hand.
     */
    private GeocodingListener listener;

    /**
     * Okhttp is a third party library to interact with the server. OkHttpClient is a factory for calls,
     * which can be used to send HTTP requests and read their responses.
     */
    private final OkHttpClient client = new OkHttpClient();

    public Geocoder() {
        json = "";
    }

    public void setResponseListener(GeocodingListener listener) {
        this.listener = listener;
    }

    @Override
    protected LatLng doInBackground(String... params) {
        LatLng geocoded = new LatLng(0,0);

        //If nothing to geo code
        if (params[0].equals("")) return geocoded;

        try {
            Request request = new Request.Builder().url(UrlManager.getGeoCodingApiUrl(params[0])).build();
            json = client.newCall(request).execute().body().string();
            if (!json.equals("")) {
                JSONObject object = new JSONObject(json);
                if (object.getString("status").equals("OK")) {
                    JSONArray array = object.getJSONArray("results");
                    if (array.length() > 0) {
                        JSONObject data = array.getJSONObject(0);
                        JSONObject location = data.getJSONObject("geometry").getJSONObject("location");
                        geocoded = new LatLng(location.getDouble("lat"), location.getDouble("lng"));
                    } else {
                        if (listener != null) listener.onRequestFailure(new CorruptedResponseException(EMPTY_ARRAY));
                    }
                } else {
                    if (listener != null) listener.onRequestFailure(new CorruptedResponseException(STATUS_NOT_OK));
                }
            } else {
                if (listener != null) listener.onRequestFailure(new CorruptedResponseException(NULL_RESPONSE));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (listener != null) listener.onRequestFailure(e);
        }
        return geocoded;
    }

    @Override
    protected void onPostExecute(LatLng latLng) {
        super.onPostExecute(latLng);
        if (listener != null) listener.onRequestCompleted(json, latLng);
    }

    /**
     * An interface to publish results in the caller classes. By implementing this,
     * end user of the module can access final results.
     */
    public interface GeocodingListener extends ResponseListener {
        /**
         * This method will be invoked on a successful http request.
         * @param json
         * It is the response received from the server after request.
         * @param latLng
         * It is result after JSON parsing
         * @see LatLng
         */
        void onRequestCompleted(String json, LatLng latLng);
    }
}