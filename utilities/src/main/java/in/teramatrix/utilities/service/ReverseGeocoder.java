package in.teramatrix.utilities.service;

import android.os.AsyncTask;
import android.text.TextUtils;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import in.teramatrix.utilities.ResponseListener;
import in.teramatrix.utilities.exception.CorruptedResponseException;
import in.teramatrix.utilities.model.Address;
import in.teramatrix.utilities.util.UrlManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static in.teramatrix.utilities.exception.CorruptedResponseException.NULL_RESPONSE;
import static in.teramatrix.utilities.exception.CorruptedResponseException.STATUS_NOT_OK;

/**
 * A class for handling reverse geocoding. Reverse geocoding is the process of transforming a (latitude, longitude) coordinate into
 * a (partial) address. The amount of detail in a reverse geocoded location description may vary, for example one might contain the
 * full street address of the closest building, while another might contain only a city name and postal code.. To get LatLng of specified
 * address, we are interacting to the Google Server (callling Api). Here we are broadcasting results in {@link in.teramatrix.utilities.service.ReverseGeocoder.ReverseGeocodingListener}
 * after parsing JSON. Result will be bound in {@link Address}.
 * @author Mohsin Khan
 * @date 1/5/2016
 */

public class ReverseGeocoder extends AsyncTask<LatLng, Void, Address> {

    /**
     * Server response without parsing
     */
    private String json;

    /**
     * Result of this service will be stored in this instance.
     */
    private Address address;

    /**
     * This will publish results to your app module. Just pass an implemented version of this listener in this class
     * and results will be in your hand.
     */
    private ReverseGeocodingListener listener;

    /**
     * Okhttp is a third party library to interact with the server. OkHttpClient is a factory for calls,
     * which can be used to send HTTP requests and read their responses.
     */
    private final OkHttpClient client = new OkHttpClient();

    public ReverseGeocoder() {
        json = "";
        address = new Address();
    }

    public ReverseGeocoder setResponseListener(ReverseGeocodingListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    protected Address doInBackground(LatLng... params) {
        try {
            Request request = new Request.Builder().url(UrlManager.getReverseGeoCodingApiUrl(params[0])).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                //Parsing JSON
                json = response.body().string();
                JSONObject object = new JSONObject(json);
                String Status = object.getString("status");
                if (Status.equalsIgnoreCase("OK")) {
                    //If every thing is alright
                    JSONArray Results = object.getJSONArray("results");
                    JSONObject zero = Results.getJSONObject(0);
                    JSONArray address_components = zero.getJSONArray("address_components");

                    for (int i = 0; i < address_components.length(); i++) {
                        JSONObject zero2 = address_components.getJSONObject(i);
                        String long_name = zero2.getString("long_name");
                        JSONArray mtypes = zero2.getJSONArray("types");
                        String Type = mtypes.getString(0);
                        if (TextUtils.isEmpty(long_name) == false || !long_name.equals(null) || long_name.length() > 0 || long_name != "") {
                            if (Type.equalsIgnoreCase("street_number")) {
                                address.setAddressOne(long_name + " ");
                            } else if (Type.equalsIgnoreCase("route")) {
                                address.setAddressOne(address.getAddressOne() + long_name);
                            } else if (Type.equalsIgnoreCase("sublocality")) {
                                address.setAddressTwo(long_name);
                            } else if (Type.equalsIgnoreCase("locality")) {
                                address.setCity(long_name);
                            } else if (Type.equalsIgnoreCase("administrative_area_level_2")) {
                                address.setDistrict(long_name);
                            } else if (Type.equalsIgnoreCase("administrative_area_level_1")) {
                                address.setState(long_name);
                            } else if (Type.equalsIgnoreCase("country")) {
                                address.setCountry(long_name);
                            } else if (Type.equalsIgnoreCase("postal_code")) {
                                address.setPin(long_name);
                            }
                        }
                    }
                } else {
                    throw new CorruptedResponseException(STATUS_NOT_OK);
                }
            } else {
                throw new CorruptedResponseException(NULL_RESPONSE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                listener.onRequestFailure(e);
                listener = null;
            }
        }
        return address;
    }

    @Override
    protected void onPostExecute(Address address) {
        super.onPostExecute(address);
        if (listener != null) listener.onRequestCompleted(json, address);
    }

    /**
     * An interface to publish results in the caller classes. By implementing this,
     * end user of the module can access final results.
     */
    public interface ReverseGeocodingListener extends ResponseListener {
        /**
         * This method will be invoked on a successful http request.
         * @param json
         * It is the raw json response received from the server after request.
         * @param address
         * It is result after JSON parsing
         * @see Address
         */
        void onRequestCompleted(String json, Address address);
    }
}