package marocco;

import com.strazzullo_marocco_sibilla_marin.app.remote.CustomerService;
import sibilla.Cuisine;
import sibilla.LocationSearchResult;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Manual test entry point for the location search filters.
 * Connects to the RMI registry exposed by the server and runs a few
 * {@link SearchFilter} scenarios against {@link CustomerService#searchLocations}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class SearchFilterTestMain {

    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    private static final String SERVICE_NAME = "CustomerService";

    /**
     * Function to run the search filter test scenarios.
     *
     * @param args unused
     * @throws Exception if the RMI lookup or a remote call fails
     */
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 && !args[0].isBlank() ? args[0] : HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : PORT;
        String serviceName = args.length > 2 && !args[2].isBlank() ? args[2] : SERVICE_NAME;

        Registry registry = LocateRegistry.getRegistry(host, port);
        CustomerService customerService = (CustomerService) registry.lookup(serviceName);

        runScenario("no filter", customerService, new SearchFilter.Builder().build());
        runScenario("city = Varese", customerService, new SearchFilter.Builder().city("Varese").build());
        runScenario("cuisine = italian", customerService, new SearchFilter.Builder().cuisineType(Cuisine.italian).build());
        runScenario("delivery = true", customerService, new SearchFilter.Builder().delivery(true).build());
        runScenario("min rating = 4.0", customerService, new SearchFilter.Builder().minRating(4.0).build());
        runScenario("open monday at 12:00", customerService,
                new SearchFilter.Builder().openDay(sibilla.Day.monday).openTime("12:00").build());
        runScenario("distance 10km from coordinates 45.8,8.8", customerService,
                new SearchFilter.Builder().distance(45.8, 8.8, 10.0).build());
        runScenario("distance 10km from address", customerService,
                new SearchFilter.Builder().distanceFromAddress("Varese, Italy", 10.0).build());
        runScenario("paged results", customerService,
                new SearchFilter.Builder().page(0, 5).build());
    }

    private static void runScenario(String label, CustomerService customerService, SearchFilter filter) throws Exception {
        System.out.println("=== " + label + " ===");
        System.out.println("filter: " + filter);
        List<LocationSearchResult> results = customerService.searchLocations(filter);
        System.out.println("results: " + results.size());
        for (LocationSearchResult result : results) {
            System.out.println(" - " + result.location().getId() + " | " + result.location().getCity() + " | "
                    + result.location().getAddress() + " | restaurant=" + result.restaurantName()
                    + " | cuisine=" + result.restaurantCuisine()
                    + " | rating=" + result.averageRating()
                    + " (" + result.reviewCount() + " reviews)"
                    + (result.distanceKm() != null ? " | distance=" + result.distanceKm() + "km" : ""));
        }
        System.out.println();
    }
}
