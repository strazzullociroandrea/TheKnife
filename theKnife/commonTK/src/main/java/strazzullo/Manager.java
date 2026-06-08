import sibilla.Restaurant;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Manager in the system.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class Manager extends User {

    /**
     * List of favorite restaurants of the manager. This list can be modified by the manager to add or remove restaurants.
     */
    private List<Restaurant> restaurants;

    /**
     * Manager constructor to create a Manager with all parameters, without date of birth and id
     *
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     */
    public Manager(String name, String surname, String email, String password, String domicile) {
        super(name, surname, email, password, domicile);
        this.restaurants = new ArrayList<>();
    }

    /**
     * Manager constructor to create a Manager with all parameters, including date of birth, without id
     *
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     * @param dateOfBirth
     */
    public Manager(String name, String surname, String email, String password, String domicile, String dateOfBirth) {
        super(name, surname, email, password, domicile, dateOfBirth);
        this.restaurants = new ArrayList<>();
    }

    /**
     * Manager constructor to create a Manager with all parameters, including date of birth and id
     *
     * @param id
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     * @param dateOfBirth
     */
    public Manager(String id, String name, String surname, String email, String password, String domicile, String dateOfBirth) {
        super(id, name, surname, email, password, domicile, dateOfBirth);
        this.restaurants = new ArrayList<>();
    }

    /**
     * Manager constructor to create a Manager with all parameters, without date of birth, including id
     *
     * @param id
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     */
    public Manager(String id, String name, String surname, String email, String password, String domicile) {
        super(id, name, surname, email, password, domicile);
        this.restaurants = new ArrayList<>();
    }

    /**
     * Returns the specific role of this user.
     *
     * @return "Gestore"
     */
    @Override
    public String getRole() {
        return "Gestore";
    }


    /**
     * Function to add a restaurant to the list of restaurants of the manager. This function is used when the manager creates a new restaurant.
     *
     * @param restaurant
     */
    public void addRestaurant(Restaurant restaurant) {
        this.restaurants.add(restaurant);
    }


    public void addPhoto(Restaurant restaurant, String url) {
        System.out.println("ADD PHOTO TODO - CRUD");
    }

    public void addReview(Restaurant restaurant, String review) {
        System.out.println("ADD REVIEW TODO - CRUD");
    }

    public void manageReservation() {
        System.out.println("MANAGE RESERVATION TODO - CRUD");
    }
}