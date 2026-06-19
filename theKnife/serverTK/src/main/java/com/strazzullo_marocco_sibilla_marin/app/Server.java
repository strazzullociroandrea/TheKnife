package com.strazzullo_marocco_sibilla_marin.app;

import com.strazzullo_marocco_sibilla_marin.app.service.CustomerServiceImpl;
import com.strazzullo_marocco_sibilla_marin.app.config.DotEnv;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;


public class Server {

    public static final int RMI_PORT = 1099;
    public static final String CUSTOMER_SERVICE_NAME = "CustomerService";

    private static String getData(String message, Scanner s) {
        System.out.println(message);
        String data = s.nextLine();

        while (data == null || data.isEmpty()) {
            System.err.println("E' stato inserito un valore non adatto.");
            System.out.println(message);
            data = s.nextLine();
        }

        return data;
    }

    public static void main(String[] args) {

        System.out.println("Avvio del progetto in corso...");
        System.out.println("Inserisci i dati per stabilire la connessione al database postgre");

        try (Scanner s = new Scanner(System.in)) {

            /*
            String url = getData("Inserisci l'url di connessione: ", s);
            String username = getData("Inserisci l'utente: ", s);
            String password = getData("Inserisci la password: ", s);
            */
            String url = DotEnv.get("DATABASE_URL");
            String username = DotEnv.get("USER_DB");
            String password = DotEnv.get("PASS_DB");
            DBConnectionPool.getInstance(url, username, password);

            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind(CUSTOMER_SERVICE_NAME, new CustomerServiceImpl());
            System.out.println("CustomerService bound on RMI registry, port " + RMI_PORT);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
