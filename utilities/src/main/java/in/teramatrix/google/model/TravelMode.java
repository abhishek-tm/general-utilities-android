package in.teramatrix.google.model;

/**
 * For the calculation of distances and routes, you may specify the transportation mode to use.
 * By default, distances are calculated for driving mode. This value may only be specified if the request includes an API key or
 * a Google Maps APIs Premium Plan client ID If you set the mode to transit you can optionally specify either a departure_time or an arrival_time.
 * If neither time is specified, the departure_time defaults to now (that is, the departure time defaults to the current time).
 * You can also optionally include a transit_mode and/or a transit_routing_preference.
 * @author Mohsin Khan
 * @date 3/22/2016
 */
@SuppressWarnings("unused")
public final class TravelMode {
    /**
     * indicates distance calculation using the road network
     */
    public static final String MODE_DRIVING = "driving";
    /**
     * requests distance calculation for walking via pedestrian paths & sidewalks (where available)
     */
    public static final String MODE_WALKING = "walking";
    /**
     * requests distance calculation via public transit routes (where available)
     */
    public static final String MODE_TRANSIT = "transit";
    /**
     * requests distance calculation for bicycling via bicycle paths & preferred streets (where available)
     */
    public static final String MODE_BICYCLING = "bicycling";
}
