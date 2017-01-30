package in.teramatrix.google.service;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import in.teramatrix.google.ResponseListener;
import in.teramatrix.google.exception.CorruptedResponseException;
import in.teramatrix.google.model.Place;
import in.teramatrix.google.util.UrlManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static in.teramatrix.google.exception.CorruptedResponseException.NULL_RESPONSE;
import static in.teramatrix.google.exception.CorruptedResponseException.STATUS_NOT_OK;

/**
 * This class is structured to use Google's Places API using Builder Pattern. Google Places Api will find the places around the location
 * you passed in the api parameters. Although Places API has more features but this class is only constructed to explore places around a particular location.
 * Full description : <a href="https://developers.google.com/places/web-service/search">Google Places API Web Service</a>
 * @author Mohsin Khan
 * @date 1/5/2016
 */
@SuppressWarnings("unused")
public class PlacesExplorer extends AsyncTask<String, Void, ArrayList<Place>> {

    /**
     * Server response without parsing
     */
    private String json;

    /**
     *  Defines the distance (in meters) within which to return place results.
     *  The maximum allowed radius is 50 000 meters. Note that radius must not be included if rankby=distance
     *  (described under Optional parameters below) is specified.
     */
    private int radius;

    /**
     * The Google Places API previously required that you include the sensor parameter to indicate
     * whether your application used a sensor to determine the user's location. This parameter is no longer required.
     */
    private boolean sensor;

    /**
     * Specifies the order in which results are listed. Possible values are:
     * <br/>
     * <b>prominence (default) :</b> This option sorts results based on their importance. Ranking will favor prominent places within
     * the specified area. Prominence can be affected by a place's ranking in Google's index, global popularity,
     * and other factors.
     * <br/>
     * <b>distance :</b> This option biases search results in ascending order by their distance from the specified location.
     * When distance is specified, one or more of keyword, name, or type is required.
     */
    private String rankBy;

    /**
     *  Your application's API key. This key identifies your application for purposes of quota management and
     *  so that places added from your application are made immediately available to your app.
     */
    private String key;

    /**
     * The latitude/longitude around which to retrieve place information. This must be specified as latitude,longitude.
     */
    private LatLng location;

    /**
     * This will publish results to your app module. Just pass an implemented version of this listener in this class
     * and results will be in your hand.
     */
    private PlaceExplorerListener listener;

    /**
     * Okhttp is a third party library to interact with the server. OkHttpClient is a factory for calls,
     * which can be used to send HTTP requests and read their responses.
     */
    private final OkHttpClient client = new OkHttpClient();

    public PlacesExplorer() {
        this.json = "";
        //Default Setting
        this.radius = 10000;
        this.sensor = false;
        this.rankBy = "distance";
    }

    public PlacesExplorer setResponseListener(PlaceExplorerListener listener) {
        this.listener = listener;
        return this;
    }

    public PlacesExplorer setRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public PlacesExplorer setSensor(boolean sensor) {
        this.sensor = sensor;
        return this;
    }

    public PlacesExplorer setRankBy(String rankBy) {
        this.rankBy = rankBy;
        return this;
    }

    public PlacesExplorer setKey(String key) {
        this.key = key;
        return this;
    }

    public PlacesExplorer setLocation(LatLng location) {
        this.location = location;
        return this;
    }

    /**
     * It is just like build method of Builder Pattern. It will execute this {@link AsyncTask}
     * @param places to be searched/explore for example "bar", "restaurant" <br/>
     *               See <a href="https://developers.google.com/places/supported_types#table1">full list of supported types</a>
     */
    public void explore(String ... places) {
        //If nothing to search/explore
        if (key == null || key.equals(""))
            throw new NullPointerException("Browser key can not be null");
        //If location is not setup by caller
        if (location == null)
            throw new NullPointerException("Location can not be null");

        this.execute(places);
    }

    @Override
    protected ArrayList<Place> doInBackground(String... params) {
        ArrayList<Place> places = new ArrayList<>();

        //If nothing to search then returning empty list instead of any exception
        if (params.length <= 0) return places;

        try {
            //Building request and making call
            Request request = new Request.Builder().url(UrlManager.getPlacesApiUrl
                    (location, params, radius, rankBy, sensor, key)).build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                //Parsing JSON response
                json = response.body().string();
                JSONObject object = new JSONObject(json);
                if (object.getString("status").equalsIgnoreCase("OK")) {
                    JSONArray results = object.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject placeObject = results.getJSONObject(i);
                        JSONObject location = placeObject.getJSONObject("geometry").getJSONObject("location");
                        Place place = new Place();
                        place.setId(placeObject.getString("id"));
                        place.setPlaceId(placeObject.getString("place_id"));
                        place.setName(placeObject.getString("name"));
                        place.setType(getPlaceType(params, placeObject.getJSONArray("types")));
                        place.setIcon(placeObject.getString("icon"));
                        place.setVicinity(placeObject.getString("vicinity"));
                        place.setLocation(new LatLng(location.getDouble("lat"), location.getDouble("lng")));
                        places.add(place);
                    }
                } else {
                    //If Google's API status is not ok
                    if (listener != null)
                        listener.onRequestFailure(new CorruptedResponseException(STATUS_NOT_OK));
                }
            } else {
                //If response is not successful
                if (listener != null)
                    listener.onRequestFailure(new CorruptedResponseException(NULL_RESPONSE));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            if (listener != null) listener.onRequestFailure(e);
        }
        return places;
    }

    @Override
    protected void onPostExecute(ArrayList<Place> places) {
        super.onPostExecute(places);
        if (listener != null) listener.onRequestCompleted(json, places);
    }

    /**
     * This method will detect the type of place. It will find out the common string between two arrays.
     * @param sent the array that will be sent to the server
     * @param received the array that is received from the server
     * @return common item in both arrays
     * @throws JSONException this will parse json array, so exception may be arise.
     */
    private String getPlaceType(String [] sent, JSONArray received) throws JSONException {
        for (String s : sent)
            for (int i = 0; i < received.length(); i++)
                if (s.equals(received.getString(i)))
                    return s;

        return "";
    }

    /**
     * An interface to publish results in the caller classes. By implementing this,
     * end user of the module can access final results.
     */
    public interface PlaceExplorerListener extends ResponseListener {
        /**
         * This method will be invoked on a successful http request.
         * @param json
         * It is the response received from the server after request.
         * @param places
         * an {@link ArrayList} of {@link Place}
         * @see Place
         */
        void onRequestCompleted(String json, ArrayList<Place> places);
    }
}