package in.teramatrix.google.util;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * A collection of all the APIs used in this project. In case of any updates in URL, only this file can be modified. At this time,
 * this file contains following Apis...
 * <ul type="square">
 *     <li>Geocoding</li>
 *     <li>Reverse Geocoding</li>
 *     <li>Places Api</li>
 *     <li>Directions Api</li>
 *     <li>Distance Matrix Api</li>
 * </ul>
 *
 * @author Mohsin Khan
 * @date 3/21/2016
 */
public class UrlManager {
    /**
     * This method will construct the URL ro approach to the Google Server for Google's Geocoding API.
     * <br/>
     * See <a href="https://developers.google.com/maps/documentation/geocoding/intro#BYB">Geocoding</a>
     * @param address to be goecoded
     * @return url
     */
    public static String getGeoCodingApiUrl(String address) {
        return "https://maps.google.com/maps/api/geocode/json?address=" + address;
    }

    /**
     * This method will construct the URL ro approach to the Google Server for Google's Places API.
     * <br/>
     * See <a href="https://developers.google.com/places/place-id#example-using-the-places-api-web-service">Google Places API</a>
     * @return url
     * @throws UnsupportedEncodingException
     */
    public static String getPlacesApiUrl(LatLng location, String [] params, int radius, String rankBy, boolean sensor, String key) throws UnsupportedEncodingException {
        return "https://maps.googleapis.com/maps/api/place/search/json"
                + "?location=" + URLEncoder.encode(String.valueOf(location.latitude) + "," + String.valueOf(location.longitude), "UTF-8")
                + "&type=" + URLEncoder.encode(getAppendedString("|", params), "UTF-8")
                + "&radius=" + URLEncoder.encode(String.valueOf(radius), "UTF-8")
                + "&rankBy=" + URLEncoder.encode(rankBy, "UTF-8")
                + "&sensor=" + URLEncoder.encode(sensor?"true":"false", "UTF-8")
                + "&key=" + URLEncoder.encode(key, "UTF-8");
    }

    /**
     * This method will construct the URL ro approach to the Google Server for Google's Geocoding API.
     * <br/>
     * See <a href="https://developers.google.com/maps/documentation/geocoding/intro#ReverseGeocoding">Reverse Geocoding</a>
     * @param location to be reverse geocoded
     * @return url
     */
    public static String getReverseGeoCodingApiUrl(LatLng location) {
        return "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                + location.latitude + "," + location.longitude + "&sensor=true";
    }

    /**
     * This method will construct the URL to approach to the Google Server for Google's Direction Matrix API.
     * <br/>
     * See <a href="https://developers.google.com/maps/documentation/distance-matrix/intro#Introduction">Google Maps Distance Matrix API</a>
     * @param points an array of waypoints
     * @return url
     */
    public static String getDirectionApiUrl(LatLng origin, LatLng destination, boolean sensor, String mode, boolean alternatives, LatLng [] points) {
        String waypoints = "";
        for (LatLng point : points)
            waypoints += String.valueOf(point.latitude) + "," + String.valueOf(point.longitude) + "|";

        String url = "http://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + String.valueOf(origin.latitude) + "," + String.valueOf(origin.longitude) +
                "&destination=" + String.valueOf(destination.latitude) + "," + String.valueOf(destination.longitude) +
                ((waypoints.equals("")) ? "" : "&waypoints=" + waypoints.substring(0, waypoints.length() -1)) +
                "&sensor=" + sensor +
                "&mode=" + mode +
                "&alternatives=" + alternatives;
        Log.d("DIRECTION_API", url);
        return url;
    }

    /**
     * This method will construct URL if neither server is available nor crypto key.
     * <br/>
     * See <a href="https://developers.google.com/maps/documentation/distance-matrix/get-api-key">Get a Key/Authentication</a>
     * @param origins from where, all distances will be calculated
     * @param destinations end points
     * @return url
     */
    public static String getDistanceMatrixUrl(String [] origins, String [] destinations, String mode) throws UnsupportedEncodingException {
        return "https://maps.googleapis.com"
                + "/maps/api/distancematrix/json"
                + "?origins=" + URLEncoder.encode(getAppendedString("|", origins), "UTF-8")
                + "&destinations=" + URLEncoder.encode(getAppendedString("|", destinations), "UTF-8")
                + "&mode=" + mode
                + "&language=en";
    }

    /**
     * This method will construct URL for free users of this google api.
     * <br/>
     * See <a href="https://developers.google.com/maps/documentation/distance-matrix/get-api-key#get-an-api-key">Get an API Key</a>
     * @param origins from where, all distances will be calculated
     * @param destinations end points
     * @return url
     */
    public static String getDistanceMatrixUrl(String [] origins, String [] destinations, String mode, String serverKey) throws UnsupportedEncodingException {
        return "https://maps.googleapis.com"
                + "/maps/api/distancematrix/json"
                + "?origins=" + URLEncoder.encode(getAppendedString("|", origins), "UTF-8")
                + "&destinations=" + URLEncoder.encode(getAppendedString("|", destinations), "UTF-8")
                + "&mode=" + mode
                + "&language=en"
                + "&key=" + serverKey;
    }

    /**
     * This method will construct URL for premium users of this google api.
     * <br/>
     * See <a href="https://developers.google.com/maps/documentation/distance-matrix/get-api-key#premium-auth">Get an API Key</a>
     * @param origins from where, all distances will be calculated
     * @param destinations end points
     * @return url
     */
    public static String getDistanceMatrixUrl(String [] origins, String [] destinations, String mode, String clientId, String cryptoKey) throws IOException, NoSuchAlgorithmException, InvalidKeyException, URISyntaxException {
        String rawUrl = "https://maps.googleapis.com"
                + "/maps/api/distancematrix/json"
                + "?origins=" + URLEncoder.encode(getAppendedString("|", origins), "UTF-8")
                + "&destinations=" + URLEncoder.encode(getAppendedString("|", destinations), "UTF-8")
                + "&mode=" + mode
                + "&language=en"
                + "&client=" + clientId;

        //Obtaining a Url instance using url string
        URL url = new URL(rawUrl);

        //this is url signer that will generate signature
        UrlSigner signer = new UrlSigner(cryptoKey);

        //Concatenating the signature with url
        return rawUrl + signer.signRequest(url.getPath(), url.getQuery());
    }

    /**
     * This method will form all the array elements separated using seprator like pipeline "|".
     * For example if array consists of "hotel", "restaurant", "bar" then these items will be converted as "hotel|restaurant|bar"
     * @param params array of place types to be search
     * @return a single string of all items
     */
    private static String getAppendedString(String separator, String... params) {
        String str = "";
        for (String param : params)
            str += separator + param.trim();
        return str.substring(1, str.length());
    }
}
