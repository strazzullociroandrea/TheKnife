package sibilla;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/* un ristorante ha: id, nome, tipo cucina, array Utenti (owners), array di Sedi*/
public class Restaurant implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private Cuisine cuisine;
    private List<User> owners;
    private List<Location> locations;

    public Restaurant(){
         id = UUID.randomUUID().toString();
         name = "";
         cuisine = null;
         owners = new ArrayList<>();
         locations = new ArrayList<>();
    }

    public Restaurant(String id, String name, Cuisine cuisine, List<User> owners, List<Location> locations){
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.owners = new ArrayList<>(owners); // The list already has some owners
        this.locations = new ArrayList<>(locations);
    }

    public Restaurant(String id, String name, Cuisine cuisine, User owner, List<Location> locations){
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.owners = new ArrayList<>();    // The list is empty
        this.owners.add(owner);             // Adding one owner to the list
        this.locations = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cuisine getCuisine() {
        return cuisine;
    }

    public void setCuisine(Cuisine cuisine) {
        this.cuisine = cuisine;
    }

    public List<User> getOwners() {
        return owners;
    }

    public void setOwners(List<User> owners) {
        this.owners = owners;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }
}
