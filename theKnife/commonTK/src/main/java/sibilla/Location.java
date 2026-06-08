/**
 *
 * Represents a physical location of a restaurant in the TheKnife system.
 *
 * Each location contains geographical and operational information for a
 * specific restaurant branch, including address details and capacity.
 *
 * @author Sibilla
 * @version 1.0
 */

package sibilla;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Location implements java.io.Serializable {
    @Serial // Compiler annotation
    private static final long serialVersionUID = 1L;

    private String id;
    private String country, city, address;
    private Float latitude, longitude;
    private int priceRange;
    private boolean delivery, takeaway;
    private int maxCapacity;
    private boolean vegetarianMenu, veganMenu, glutenFreeMenu;
    private Map<Day, String> openingTimes;

    /**
     * An empty constructor for a location. Nothing is passed to it.
     */
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

    /**
     * Declares a location with all parameters.
     *
     * @param id                    location's id
     * @param country               location's country
     * @param city                  location's city
     * @param address               location's address
     * @param latitude              location's latitude coordinate
     * @param longitude             location's longitude coordinate
     * @param priceRange            location's price range
     * @param delivery              whether delivery is available
     * @param takeaway              whether takeaway is available
     * @param maxCapacity           location's maximum seating capacity
     * @param vegetarianMenu        whether vegetarian menu is available
     * @param veganMenu             whether vegan menu is available
     * @param glutenFreeMenu        whether gluten-free menu is available
     * @param openingTimes          map of opening times by day
     */
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

    /**
     * getId
     *
     * @return returns id
     */
    public String getId() {
        return id;
    }

    /**
     * setId
     *
     * @param id sets id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * getCountry
     *
     * @return returns country
     */
    public String getCountry() {
        return country;
    }

    /**
     * setCountry
     *
     * @param country sets country
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * getCity
     *
     * @return returns city
     */
    public String getCity() {
        return city;
    }

    /**
     * setCity
     *
     * @param city sets city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * getAddress
     *
     * @return returns address
     */
    public String getAddress() {
        return address;
    }

    /**
     * setAddress
     *
     * @param address sets address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * getLatitude
     *
     * @return returns latitude
     */
    public Float getLatitude() {
        return latitude;
    }

    /**
     * setLatitude
     *
     * @param latitude sets latitude
     */
    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    /**
     * getLongitude
     *
     * @return returns longitude
     */
    public Float getLongitude() {
        return longitude;
    }

    /**
     * setLongitude
     *
     * @param longitude sets longitude
     */
    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    /**
     * getPriceRange
     *
     * @return returns priceRange
     */
    public int getPriceRange() {
        return priceRange;
    }

    /**
     * setPriceRange
     *
     * @param priceRange sets priceRange
     */
    public void setPriceRange(int priceRange) {
        this.priceRange = priceRange;
    }

    /**
     * isDelivery
     *
     * @return returns delivery
     */
    public boolean isDelivery() {
        return delivery;
    }

    /**
     * setDelivery
     *
     * @param delivery sets delivery
     */
    public void setDelivery(boolean delivery) {
        this.delivery = delivery;
    }

    /**
     * isTakeaway
     *
     * @return returns takeaway
     */
    public boolean isTakeaway() {
        return takeaway;
    }

    /**
     * setTakeaway
     *
     * @param takeaway sets takeaway
     */
    public void setTakeaway(boolean takeaway) {
        this.takeaway = takeaway;
    }

    /**
     * getMaxCapacity
     *
     * @return returns maxCapacity
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * setMaxCapacity
     *
     * @param maxCapacity sets maxCapacity
     */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    /**
     * isVegetarianMenu
     *
     * @return returns vegetarianMenu
     */
    public boolean isVegetarianMenu() {
        return vegetarianMenu;
    }

    /**
     * setVegetarianMenu
     *
     * @param vegetarianMenu sets vegetarianMenu
     */
    public void setVegetarianMenu(boolean vegetarianMenu) {
        this.vegetarianMenu = vegetarianMenu;
    }

    /**
     * isVeganMenu
     *
     * @return returns veganMenu
     */
    public boolean isVeganMenu() {
        return veganMenu;
    }

    /**
     * setVeganMenu
     *
     * @param veganMenu sets veganMenu
     */
    public void setVeganMenu(boolean veganMenu) {
        this.veganMenu = veganMenu;
    }

    /**
     * isGlutenFreeMenu
     *
     * @return returns glutenFreeMenu
     */
    public boolean isGlutenFreeMenu() {
        return glutenFreeMenu;
    }

    /**
     * setGlutenFreeMenu
     *
     * @param glutenFreeMenu sets glutenFreeMenu
     */
    public void setGlutenFreeMenu(boolean glutenFreeMenu) {
        this.glutenFreeMenu = glutenFreeMenu;
    }

    /**
     * getOpeningTimes
     *
     * @return returns openingTimes
     */
    public Map<Day, String> getOpeningTimes() {
        return openingTimes;
    }

    /**
     * setOpeningTimes
     *
     * @param openingTimes sets openingTimes
     */
    public void setOpeningTimes(Map<Day, String> openingTimes) {
        this.openingTimes = openingTimes;
    }
}

