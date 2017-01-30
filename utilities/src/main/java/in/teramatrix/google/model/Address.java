package in.teramatrix.google.model;

/**
 * Defined for reverse geo-coding. Google's Geo coded data will be bound in this model. Model simply
 * contains some basic entities to present location like pin code, city name, state name country etc.
 * This class is used in {@link in.teramatrix.google.service.ReverseGeocoder}
 * @author Mohsin Khan
 * @date 3/18/2016
 */
@SuppressWarnings("unused")
public class Address {
    private String pin;
    private String city;
    private String state;
    private String country;
    private String district;
    private String addressOne;
    private String addressTwo;

    public Address() {

    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getAddressOne() {
        return addressOne;
    }

    public void setAddressOne(String addressOne) {
        this.addressOne = addressOne;
    }

    public void setAddressTwo(String addressTwo) {
        this.addressTwo = addressTwo;
    }


    @Override
    public String toString() {
        return filter(addressOne)
                + filter(addressTwo)
                + filter(city)
                + filter(district)
                + filter(state)
                + filter(country)
                +pin;
    }

    private String filter(String value) {
        if (value != null)
            return (value.equals("null")) ? "" : value + ", ";
        else
            return "";
    }
}
