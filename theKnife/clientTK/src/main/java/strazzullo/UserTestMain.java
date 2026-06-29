package strazzullo;

import com.strazzullo_marocco_sibilla_marin.app.remote.AuthService;
import com.strazzullo_marocco_sibilla_marin.app.remote.LoginResult;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class UserTestMain {

    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    private static final String SERVICE_NAME = "AuthService";

    public static void main(String[] args) {
        try {
            System.out.println("UserTestMain in execution.");
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            AuthService authService = (AuthService) registry.lookup(SERVICE_NAME);

            System.out.println("Test 1: Create a new user - manager");
            User manager = new Manager(null, "Manager", "Test", "manager@gmail.com", "manager123", "Via Roma 1, Varese", "1980-01-01");
            authService.register(manager, "manager123");
            LoginResult managerResult = authService.login("manager@gmail.com", "manager123");
            if (managerResult != null && managerResult.getUser() instanceof Manager) {
                System.out.println("Manager created successfully: " + managerResult.getUser());
                authService.logout(managerResult.getSessionToken());
                System.out.println("Logout del manager");
            } else {
                System.out.println("Failed to create manager.");
            }

            System.out.println("Test 2: Create a new user - customer");
            User client = new Client(null, "Client", "Test", "client@gmail.com", "client123", "Via Roma 100, Varese");
            authService.register(client, "client123");
            LoginResult clientResult = authService.login("client@gmail.com", "client123");
            if (clientResult != null && clientResult.getUser() instanceof Client) {
                System.out.println("Client created successfully: " + clientResult.getUser());
                authService.logout(clientResult.getSessionToken());
                System.out.println("Logout del cliente");
            } else {
                System.out.println("Failed to create client.");
            }

        } catch (Exception e) {
            System.err.println("An error occurred during the user test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
