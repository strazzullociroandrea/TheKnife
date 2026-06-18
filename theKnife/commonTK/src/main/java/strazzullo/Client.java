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

    private List<Restaurant> favoriteRestaurants;

    public Client(String id, String name, String surname, String email, String password, String domicile, String dateOfBirth, boolean isPasswordHashed) {
        super(id, name, surname, email, password, domicile, dateOfBirth, isPasswordHashed);
        this.favoriteRestaurants = new ArrayList<>();
    }

    public Client(String id, String name, String surname, String email, String password, String domicile, boolean isPasswordHashed) {
        this(id, name, surname, email, password, domicile, null, isPasswordHashed);
    }

    public Client(String name, String surname, String email, String password, String domicile, boolean isPasswordHashed) {
        this(null, name, surname, email, password, domicile, null, isPasswordHashed);
    }

    @Override
    public String getRole() {
        return "Cliente";
    }

    public void addFavoriteRestaurant(Restaurant restaurant) {
        if (!favoriteRestaurants.contains(restaurant)) {
            favoriteRestaurants.add(restaurant);
        }
    }

    public List<Restaurant> getFavoriteRestaurants() {
        return favoriteRestaurants;
    }

    public boolean removeFavoriteRestaurant(Restaurant restaurant) {
        return favoriteRestaurants.remove(restaurant);
    }
}