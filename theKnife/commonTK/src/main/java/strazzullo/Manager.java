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


    private List<Restaurant> restaurants;

    public Manager(String id, String name, String surname, String email, String password, String domicile, String dateOfBirth, boolean isPasswordHashed) {
        super(id, name, surname, email, password, domicile, dateOfBirth, isPasswordHashed);
        this.restaurants = new ArrayList<>();
    }

    public Manager(String id, String name, String surname, String email, String password, String domicile, boolean isPasswordHashed) {
        this(id, name, surname, email, password, domicile, null, isPasswordHashed);
    }


    public Manager(String name, String surname, String email, String password, String domicile, boolean isPasswordHashed) {
        this(null, name, surname, email, password, domicile, null, isPasswordHashed);
    }

    @Override
    public String getRole() {
        return "Gestore";
    }


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