package strazzullo;
import com.strazzullo_marocco_sibilla_marin.app.remote.AuthService;
import strazzullo.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class UserTestMain {

    private static final String HOST = "localhost";
    private static final int PORT = 1099;
    private static final String SERVICE_NAME = "AuthService";


    public static void main(String[] args) {
        try{
            System.out.println("UserTestMain in execution.");
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            AuthService authService = (AuthService) registry.lookup(SERVICE_NAME);

            System.out.println("Test 1: Create a new user - manager");

            User manager = new Manager(null, "Manager", "Test", "manager@gmail.com", "manager123", "Via Roma 1, Varese", "1980-01-01");
            authService.register(manager, "manager123");
            User createdManager = authService.login("manager@gmail.com", "manager123");
            if(createdManager instanceof Manager && createdManager.getRole().equals("manager")) {
                System.out.println("Manager created successfully: " + createdManager);
            } else {
                System.out.println("Failed to create manager.");
            }

            System.out.println("Logout del manager");
            authService.logout(manager.getId());

            System.out.println("Test 2: Create a new user - customer");


            User client = new Client(null, "Client", "Test", "client@gmail.com", "client123", "Via Roma 100, Varese");
            authService.register(client, "client123");
            User createdClient = authService.login("client@gmail.com", "client123");
            if(createdClient instanceof Client && createdClient.getRole().equals("customer")) {
                System.out.println("Client created successfully: " + createdClient);
            } else {
                System.out.println("Failed to create client.");
            }

            System.out.println("Logout del cliente");
            authService.logout(client.getId());

        }catch(Exception e){
            System.err.println("An error occurred during the user test: " + e.getMessage());
            e.printStackTrace();
        }

    }
}