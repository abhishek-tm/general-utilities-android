package in.teramatrix.utilities.service;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import in.teramatrix.utilities.ResponseListener;
import in.teramatrix.utilities.exception.CorruptedResponseException;
import in.teramatrix.utilities.model.Distance;
import in.teramatrix.utilities.model.TravelMode;
import in.teramatrix.utilities.util.UrlManager;
import in.teramatrix.utilities.util.UrlSigner;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import static in.teramatrix.utilities.exception.CorruptedResponseException.NULL_RESPONSE;
import static in.teramatrix.utilities.exception.CorruptedResponseException.STATUS_NOT_OK;

/**
 * Retrieve duration and distance values based on the recommended route between start and end points. But this class
 * has been designed to get distances/durations between a single origin and multiple destination. It is something like one to may
 * relationship.
 * See <a href="https://developers.google.com/maps/documentation/distance-matrix/">Google Maps Distance Matrix API</a>
 * @author Mohsin Khan
 * @date 21/3/2016
 */

@SuppressWarnings("unused")
public class DistanceCalculator extends AsyncTask<String, Void, ArrayList<Distance>> {
    /**
     * Server response without parsing
     */
    private String json;

    /**
     * APIs in each platform require a specific type of key. The Google Maps Distance Matrix API will only work with a Server key.
     * APIs of the same platform can use the same key. It is for free users.
     */
    private String serverKey;

    /**
     * A unique digital signature is generated using your private cryptographic key.
     * Pass this crypto key , signature will be generated here using {@link UrlSigner}
     * <br/>
     * For more details : <a href="https://developers.google.com/maps/documentation/distance-matrix/get-api-key#client-id">Get a Key/Authentication</a>
     */
    private String cryptoKey;

    /**
     * Your client ID is used to access the special features of Google Maps APIs Premium Plan.
     * All client IDs begin with a gme- prefix. Pass your client ID as the value of the client parameter.
     * <br/>
     * For more details : <a href="https://developers.google.com/maps/documentation/distance-matrix/get-api-key#client-id">Get a Key/Authentication</a>
     */
    private String clientId;

    /**
     * Specifies the mode of transport to use when calculating distance. Valid values and other request
     * details are specified in the {@link in.teramatrix.utilities.model.TravelMode} section of this document.
     */
    private String mode;

    /**
     *  One or more locations to use as the starting point for calculating travel distance and time. But this class only supports
     *  a single origin right now. It's further implementations may expand the origins.
     */
    private String origins[];

    /**
     * This will publish results to your app module. Just pass an implemented version of this listener in this class
     * and results will be in your hand.
     */
    private DistanceListener listener;

    /**
     * Okhttp is a third party library to interact with the server. OkHttpClient is a factory for calls,
     * which can be used to send HTTP requests and read their responses.
     */
    private final OkHttpClient client = new OkHttpClient();

    public DistanceCalculator() {
        this.json = "";
        this.mode = TravelMode.MODE_DRIVING;
    }

    public DistanceCalculator(String ... origins) {
        this.origins = origins;
        this.mode = TravelMode.MODE_DRIVING;
    }

    public DistanceCalculator setOrigins(String ... origins) {
        this.origins = origins;
        return this;
    }

    public DistanceCalculator setServerKey(String serverKey) {
        this.serverKey = serverKey;
        return this;
    }

    public DistanceCalculator setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public DistanceCalculator setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public DistanceCalculator setCryptoKey(String key) {
        this.cryptoKey = key;
        return this;
    }

    public DistanceCalculator setResponseListener(DistanceListener listener) {
        this.listener = listener;
        return this;
    }

    public void calculate(String ... destinations) {
        //If nothing to calculate
        if (origins == null || destinations == null)
            throw new NullPointerException("Origin or Destination can not be null");

        this.execute(destinations);
    }

    @Override
    protected ArrayList<Distance> doInBackground(String ... destinations) {
        ArrayList<Distance> distances = new ArrayList<>();

        //If nothing to process then returning an empty list
        if (origins == null || destinations == null)
            return distances;

        try {
            String url = buildUrl(destinations);
            Request request = new Request.Builder().url(url).build();
            json = client.newCall(request).execute().body().string();

            if (!json.equals("")) {
                JSONObject object = new JSONObject(json);
                if (object.getString("status").equals("OK")) {
                    JSONArray destAddresses = object.getJSONArray("destination_addresses");
                    JSONArray orgAddresses = object.getJSONArray("origin_addresses");
                    JSONArray rows = object.getJSONArray("rows");
                    for (int i = 0; i < rows.length(); i++) {
                        JSONArray elements = rows.getJSONObject(i).getJSONArray("elements");
                        for (int j = 0; j < elements.length(); j++) {
                            JSONObject element = elements.getJSONObject(j);
                            Distance distance = new Distance();
                            distance.setOrigin(orgAddresses.getString(i));
                            distance.setDestination(destAddresses.getString(j));
                            if (element.getString("status").equals("OK")) {
                                distance.setDistanceText(element.getJSONObject("distance").getString("text"));
                                distance.setDistanceValue(element.getJSONObject("distance").getInt("value"));
                                distance.setDurationText(element.getJSONObject("duration").getString("text"));
                                distance.setDurationValue(element.getJSONObject("duration").getInt("value"));
                            } else {
                                distance.setDistanceText("");
                                distance.setDistanceValue(0);
                                distance.setDurationText("");
                                distance.setDurationValue(0);
                            }
                            distances.add(distance);
                        }
                    }
                } else {
                    if (listener != null) listener.onRequestFailure(new CorruptedResponseException(STATUS_NOT_OK));
                }
            } else {
                if (listener != null) listener.onRequestFailure(new CorruptedResponseException(NULL_RESPONSE));
            }
        } catch (IOException | JSONException | NoSuchAlgorithmException | InvalidKeyException | URISyntaxException e) {
            e.printStackTrace();
            if (listener != null) listener.onRequestFailure(e);
        }
        return distances;
    }

    @Override
    protected void onPostExecute(ArrayList<Distance> distances) {
        super.onPostExecute(distances);
        if (listener != null) listener.onRequestCompleted(json, distances);
    }

    private String buildUrl(String [] destinations) throws IOException, URISyntaxException, NoSuchAlgorithmException, InvalidKeyException {
        if (clientId != null && cryptoKey != null) {
            return UrlManager.getDistanceMatrixUrl(origins, destinations, mode, clientId, cryptoKey);
        } else if (serverKey != null) {
            return UrlManager.getDistanceMatrixUrl(origins, destinations, mode, serverKey);
        } else {
            return UrlManager.getDistanceMatrixUrl(origins, destinations, mode);
        }
    }

    /**
     * An interface to publish results in the caller classes. By implementing this,
     * end user of the module can access final results.
     */
    public interface DistanceListener extends ResponseListener {
        /**
         * This method will be invoked on a successful http request.
         * @param json
         * It is the response received from the server after request.
         * @param distances
         * an {@link ArrayList} of type {@link Distance}
         * @see Distance
         */
        void onRequestCompleted(String json, ArrayList<Distance> distances);
    }
}