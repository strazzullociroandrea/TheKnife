package strazzullo;

import java.util.ArrayList;
import java.util.List;

import sibilla.Restaurant;
import strazzullo.User;

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
     * List of favorite restaurants
     */
    private List<Restaurant> favoriteRestaurants;

    /**
     * Constructor to create a Client instance using all attributes
     *
     * @param id               the user id
     * @param name             the user's name
     * @param surname          the user's surname
     * @param email            the user's email
     * @param password         the user's password
     * @param domicile         the user's domicile
     * @param dateOfBirth      the user's date of birth
     * @param isPasswordHashed is the password hashed? true = yes, false = no
     */
    public Client(String id, String name, String surname, String email, String password, String domicile, String dateOfBirth, boolean isPasswordHashed) {
        super(id, name, surname, email, password, domicile, dateOfBirth, isPasswordHashed);
        this.favoriteRestaurants = new ArrayList<>();
    }

    /**
     * Constructor to create a Client instance using all attributes, without date of birth
     *
     * @param id               the user id
     * @param name             the user's name
     * @param surname          the user's surname
     * @param email            the user's email
     * @param password         the user's password
     * @param domicile         the user's domicile
     * @param isPasswordHashed is the password hashed? true = yes, false = no
     */
    public Client(String id, String name, String surname, String email, String password, String domicile, boolean isPasswordHashed) {
        this(id, name, surname, email, password, domicile, null, isPasswordHashed);
    }

    /**
     * Constructor to create a Client instance using all attributes, without date of birth and id
     *
     * @param name             the user's name
     * @param surname          the user's surname
     * @param email            the user's email
     * @param password         the user's password
     * @param domicile         the user's domicile
     * @param isPasswordHashed is the password hashed? true = yes, false = no
     */
    public Client(String name, String surname, String email, String password, String domicile, boolean isPasswordHashed) {
        this(null, name, surname, email, password, domicile, null, isPasswordHashed);
    }

    /**
     * Function to return the user's role
     *
     * @return the user's role: "cliente"
     */
    @Override
    public String getRole() {
        return "cliente";
    }

    /**
     * Function to add a restaurant to favorites
     *
     * @param restaurant the restaurant to add to your favorites
     */
    public void addFavoriteRestaurant(Restaurant restaurant) {
        if (!favoriteRestaurants.contains(restaurant)) {
            favoriteRestaurants.add(restaurant);
        }
    }

    /**
     *
     * Function to return all your favorite restaurants
     *
     * @return List of restaurants
     */
    public List<Restaurant> getFavoriteRestaurants() {
        return favoriteRestaurants;
    }

    /**
     * Function to remove a restaurant from favorites
     *
     * @param restaurant restaurant to remove from favorites
     * @return true if completed, false otherwise
     */
    public boolean removeFavoriteRestaurant(Restaurant restaurant) {
        return favoriteRestaurants.remove(restaurant);
    }
}