package sibilla;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/* un ristorante ha: id, nome, tipo cucina, array Utenti (owners), array di Sedi*/
public class Restaurant {
    String id;
    String name;
    Cuisine cuisine;
    List<User> owners;
    List<Venues> locations;

    public Restaurant(){
         id = UUID.randomUUID().toString();
         name = "";
         cuisine = null;
         owners = new ArrayList<>();
         locations = new ArrayList<>();
    }

    public Restaurant(String id, String name, Cuisine cuisine, List<User> owners, List<Venues> locations){
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.owners = new ArrayList<>(owners);
        this.locations = new ArrayList<>(locations);
    }
    public Restaurant(String id, String name, Cuisine cuisine, User owners, List<Venues> locations){
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.owners = new ArrayList<>();
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

    public List<Venues> getLocations() {
        return locations;
    }

    public void setLocations(List<Venues> locations) {
        this.locations = locations;
    }
}
