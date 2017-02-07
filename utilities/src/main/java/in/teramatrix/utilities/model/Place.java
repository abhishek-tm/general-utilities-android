package in.teramatrix.utilities.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * This model will hold the information about a place fetched from the server by calling Google's Places Api. This is
 * used in {@link in.teramatrix.utilities.service.PlacesExplorer} to parse and bind results in a better way.
 * @author Mohsin Khan
 * @date 3/18/2016
 */
@SuppressWarnings("unused")
public class Place {
    private String id;
    private String placeId;
    private String name;
    private String type;
    private String icon;
    private LatLng location;
    private String vicinity;

    public Place() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    @Override
    public String toString() {
        return "Place{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", location=" + location +
                ", vicinity='" + vicinity + '\'' +
                '}';
    }
}
