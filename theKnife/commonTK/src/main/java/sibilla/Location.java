package sibilla;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * This class can be serialized while connecting from the server to the client
 */

public class Location implements java.io.Serializable {
    @Serial // Compiler annotation
    private static final long serialVersionUID = 1L;

    // Enum visible only in the Location class, used by the hashmap that indicates the opening times.
    public enum Day {
        monday, tuesday, wednesday, thursday, friday, saturday, sunday
    }

    private String id;
    private String country, city, address;
    private Float latitude, longitude;
    private int priceRange;
    private boolean delivery, takeaway;
    private int maxCapacity;
    private boolean vegetarianMenu, veganMenu, glutenFreeMenu;
    private Map<Day, String> openingTimes;

    public Location() {
        id = UUID.randomUUID().toString();
        country = "";
        city = "";
        address = "";
        latitude = 0.0f;
        longitude = 0.0f;
        priceRange = 0;
        delivery = false;
        takeaway = false;
        maxCapacity = 0;
        vegetarianMenu = false;
        glutenFreeMenu = false;
        openingTimes = new HashMap<>();
    }

    public Location(String id, String country, String city, String address, Float latitude, Float longitude,
                    int priceRange, boolean delivery, boolean takeaway, int maxCapacity,
                    boolean vegetarianMenu, boolean veganMenu, boolean glutenFreeMenu, Map<Day, String> openingTimes) {
        this.id = id;
        this.country = country;
        this.city = city;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.priceRange = priceRange;
        this.delivery = delivery;
        this.takeaway = takeaway;
        this.maxCapacity = maxCapacity;
        this.vegetarianMenu = vegetarianMenu;
        this.veganMenu = veganMenu;
        this.glutenFreeMenu = glutenFreeMenu;
        this.openingTimes = openingTimes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public int getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(int priceRange) {
        this.priceRange = priceRange;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    public boolean isTakeaway() {
        return takeaway;
    }

    public void setTakeaway(boolean takeaway) {
        this.takeaway = takeaway;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public boolean isVegetarianMenu() {
        return vegetarianMenu;
    }

    public void setVegetarianMenu(boolean vegetarianMenu) {
        this.vegetarianMenu = vegetarianMenu;
    }

    public boolean isVeganMenu() {
        return veganMenu;
    }

    public void setVeganMenu(boolean veganMenu) {
        this.veganMenu = veganMenu;
    }

    public boolean isGlutenFreeMenu() {
        return glutenFreeMenu;
    }

    public void setGlutenFreeMenu(boolean glutenFreeMenu) {
        this.glutenFreeMenu = glutenFreeMenu;
    }

    public Map<Day, String> getOpeningTimes() {
        return openingTimes;
    }

    public void setOpeningTimes(Map<Day, String> openingTimes) {
        this.openingTimes = openingTimes;
    }
}

