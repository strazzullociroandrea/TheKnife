package strazzullo;

import java.util.ArrayList;
import java.util.List;

import sibilla.Restaurant;

/**
 * Represents a Client (customer) in the system.
 * A client can book restaurants and save favorite ones.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class Client extends User {

    /**
     * List of favorite restaurants of the client. This list can be modified by the client to add or remove restaurants.
     */
    private List<Restaurant> favoriteRestaurants;

    /**
     * Client constructor to create a client with all parameters, without date of birth and id
     *
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     */
    public Client(String name, String surname, String email, String password, String domicile) {
        super(name, surname, email, password, domicile);
        this.favoriteRestaurants = new ArrayList<>();
    }

    /**
     * Client constructor to create a client with all parameters, including date of birth, without id
     *
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     * @param dateOfBirth
     */
    public Client(String name, String surname, String email, String password, String domicile, String dateOfBirth) {
        super(name, surname, email, password, domicile, dateOfBirth);
        this.favoriteRestaurants = new ArrayList<>();
    }

    /**
     * Client constructor to create a client with all parameters, including date of birth and id
     *
     * @param id
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     * @param dateOfBirth
     */
    public Client(String id, String name, String surname, String email, String password, String domicile, String dateOfBirth) {
        super(id, name, surname, email, password, domicile, dateOfBirth);
        this.favoriteRestaurants = new ArrayList<>();
    }

    /**
     * Client constructor to create a client with all parameters, without date of birth, including id
     *
     * @param id
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     */
    public Client(String id, String name, String surname, String email, String password, String domicile) {
        super(id, name, surname, email, password, domicile);
        this.favoriteRestaurants = new ArrayList<>();
    }


    /**
     * Returns the specific role of this user.
     *
     * @return "Cliente"
     */
    @Override
    public String getRole() {
        return "Cliente";
    }

    /**
     * Function to add a restaurant to the client's list of favorite restaurants.
     * It checks if the restaurant is already in the list to avoid duplicates.
     *
     * @param restaurant
     */
    public void addFavoriteRestaurant(Restaurant restaurant) {
        if (!favoriteRestaurants.contains(restaurant)) {
            favoriteRestaurants.add(restaurant);
        }
    }

    /**
     * Function to get all the favorite restaurants of a client.
     *
     * @return list of favorite restaurant
     */
    public List<Restaurant> getFavoriteRestaurants() {
        return favoriteRestaurants;
    }

    /**
     * Function to remove a restaurant from list of favorite restaurant.
     *
     * @param restaurant
     * @return true if ok, false if the restaurant was not in the list
     */
    public boolean removeFavoriteRestaurant(Restaurant restaurant) {
        return favoriteRestaurants.remove(restaurant);
    }
}