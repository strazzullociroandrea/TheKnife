package strazzullo;

import sibilla.Restaurant;
import strazzullo.User;

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
     * list of restaurants he manages
     */
    private List<Restaurant> restaurants;

    /**
     * Constructor to create a Manager instance using all attributes
     *
     * @param id               the user id
     * @param name             the user's name
     * @param surname          the user's surname
     * @param email            the user's email
     * @param password         the user's password
     * @param domicile         the user's domicile
     * @param dateOfBirth      the user's date of birth
     */
    public Manager(String id, String name, String surname, String email, String password, String domicile, String dateOfBirth) {
        super(id, name, surname, email, password, domicile, dateOfBirth);
        this.restaurants = new ArrayList<>();
    }

    /**
     * Constructor to create a Manager instance using all attributes, without date of birth
     *
     * @param id               the user id
     * @param name             the user's name
     * @param surname          the user's surname
     * @param email            the user's email
     * @param password         the user's password
     * @param domicile         the user's domicile
     */
    public Manager(String id, String name, String surname, String email, String password, String domicile) {
        this(id, name, surname, email, password, domicile, null);
    }

    /**
     * Constructor to create a Manager instance using all attributes, without date of birth and id
     *
     * @param name             the user's name
     * @param surname          the user's surname
     * @param email            the user's email
     * @param password         the user's password
     * @param domicile         the user's domicile
     * @param isPasswordHashed is the password hashed? true = yes, false = no
     */
    public Manager(String name, String surname, String email, String password, String domicile) {
        this(null, name, surname, email, password, domicile, null);
    }

    /**
     * Function to return the user's role
     *
     * @return the user's role: "gestore"
     */
    @Override
    public String getRole() {
        return "gestore";
    }

    /**
     * function to add a restaurant to the list of managed restaurants
     *
     * @param restaurant restaurant to add
     */
    public void addRestaurant(Restaurant restaurant) {
        this.restaurants.add(restaurant);
    }

    /**
     * function to add a photo to the restaurant
     *
     * @param restaurant Which restaurant should this photo be added to
     * @param url        the URL of the photo to add
     */
    public void addPhoto(Restaurant restaurant, String url) {
        System.out.println("ADD PHOTO TODO - CRUD");
    }

    /**
     * Function to add a review to the restaurant
     *
     * @param restaurant Which restaurant should this review be added to
     * @param review     the review to add
     */
    public void addReview(Restaurant restaurant, String review) {
        System.out.println("ADD REVIEW TODO - CRUD");
    }

    /**
     * Function to manage the reservation
     */
    public void manageReservation() {
        System.out.println("MANAGE RESERVATION TODO - CRUD");
    }
}