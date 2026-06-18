package com.strazzullo_marocco_sibilla_marin.app;

import java.util.Scanner;


public class Server {

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

            String url = getData("Inserisci l'url di connessione: ", s);
            String username = getData("Inserisci l'utente: ", s);
            String password = getData("Inserisci la password: ", s);

            DBConnectionPool.getInstance(url, username, password);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
