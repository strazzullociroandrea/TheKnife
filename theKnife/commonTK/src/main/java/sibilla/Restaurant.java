/**
 * Represents a restaurant entity within the TheKnife system.
 *
 * This class manages the core information about a restaurant including its name,
 * type of cuisine, owners, and physical locations. Each restaurant is uniquely
 * identified by an auto-generated UUID.
 *
 * @see User
 * @see Cuisine
 * @see Location
 *
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 * @version 1.0
 */

package sibilla;

import strazzullo.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Restaurant implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /** Unique identifier for the restaurant */
    private String id;
    /** Name of the restaurant */
    private String name;
    /** Type of cuisine offered by the restaurant */
    private Cuisine cuisine;
    /** List of owners managing the restaurant */
    private List<User> owners;
    /** List of physical locations where the restaurant operates */
    private List<Location> locations;

    /**
     * An empty constructor for a restaurant. Nothing is passed to it.
     */
    public Restaurant() {
        id = UUID.randomUUID().toString();
        name = "";
        cuisine = null;
        owners = new ArrayList<>();
        locations = new ArrayList<>();
    }

    /**
     * Declares a restaurant.
     *
     * @param id        restaurant's id
     * @param name      restaurant's name
     * @param cuisine   restaurant's type of cuisine
     * @param owners    list of owners
     * @param locations list of various locations of a restaurant
     */
    public Restaurant(String id, String name, Cuisine cuisine, List<User> owners, List<Location> locations) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.owners = new ArrayList<>(owners); // The list already has some owners
        this.locations = new ArrayList<>(locations);
    }

    /**
     * Declares a restaurant with only one owner
     *
     * @param id        restaurant's id
     * @param name      restaurant's name
     * @param cuisine   restaurant's type of cuisine
     * @param owner     initializes the list and adds owner
     * @param locations list of locations
     */
    public Restaurant(String id, String name, Cuisine cuisine, User owner, List<Location> locations) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.owners = new ArrayList<>();    // The list is empty
        this.owners.add(owner);             // Adding one owner to the list
        this.locations = new ArrayList<>();
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
     * getName
     *
     * @return returns name
     */
    public String getName() {
        return name;
    }

    /**
     * setName
     *
     * @param name sets name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getCuisine
     *
     * @return returns cuisine
     */
    public Cuisine getCuisine() {
        return cuisine;
    }

    /**
     * setCuisine
     *
     * @param cuisine sets cuisine
     */
    public void setCuisine(Cuisine cuisine) {
        this.cuisine = cuisine;
    }

    /**
     * getOwners
     *
     * @return returns owners
     */
    public List<User> getOwners() {
        return owners;
    }

    /**
     * setOwners
     *
     * @param owners sets owners
     */
    public void setOwners(List<User> owners) {
        this.owners = owners;
    }

    /**
     * getLocations
     *
     * @return returns locations
     */
    public List<Location> getLocations() {
        return locations;
    }

    /**
     * setLocations
     *
     * @param locations sets locations
     */
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}
