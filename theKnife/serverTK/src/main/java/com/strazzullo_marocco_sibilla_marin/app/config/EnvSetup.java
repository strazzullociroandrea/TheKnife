package com.strazzullo_marocco_sibilla_marin.app.config;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Interactive terminal setup wizard run once at server startup. If all required environment
 * variables are already present (via OS env or {@code .env} file) the wizard exits immediately
 * without printing anything. Otherwise it lists only the missing variables, prompts the operator
 * to enter each value, and writes the result to {@code .env} in the working directory so that
 * subsequent runs skip the wizard automatically.
 *
 * @version 1.0
 * @Author Marocco Stefano, 762192, VA - author of this file
 */
public final class EnvSetup {

    private static final List<EnvKey> KEYS = List.of(
            new EnvKey("DATABASE_URL",             "URL del database          (es. jdbc:postgresql://host:5432/theknife)"),
            new EnvKey("USER_DB",                  "Username del database"),
            new EnvKey("PASS_DB",                  "Password del database"),
            new EnvKey("PHOTO_S3_ENDPOINT",        "Endpoint S3               (es. https://s3.example.com)"),
            new EnvKey("PHOTO_S3_REGION",          "Regione S3                (es. us-east-1)"),
            new EnvKey("PHOTO_S3_BUCKET",          "Nome del bucket S3"),
            new EnvKey("PHOTO_S3_ACCESS_KEY",      "Access key S3"),
            new EnvKey("PHOTO_S3_SECRET_KEY",      "Secret key S3"),
            new EnvKey("PHOTO_S3_PUBLIC_BASE_URL", "URL pubblico base S3      (es. https://cdn.example.com)"),
            new EnvKey("GOOGLE_MAPS_API_KEY",      "Google Maps API key")
    );

    private EnvSetup() {}

    /**
     * Checks for missing required variables and, if any are absent, prompts the operator
     * interactively and writes the collected values to {@code .env}. Must be called before
     * any {@link DotEnv#get(String)} call that depends on the configured variables.
     */
    public static void run() {
        List<EnvKey> missing = new ArrayList<>();
        for (EnvKey key : KEYS) {
            String v = DotEnv.get(key.name);
            if (v == null || v.isBlank()) {
                missing.add(key);
            }
        }
        if (missing.isEmpty()) {
            return;
        }

        Map<String, String> existing = readExistingEnv();

        System.out.println();
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║     TheKnife Server — Configurazione iniziale ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println("Le seguenti variabili non sono state trovate.");
        System.out.println("I valori inseriti saranno salvati in .env per i prossimi avvii.");
        System.out.println();

        Scanner scanner = new Scanner(System.in);
        for (EnvKey key : missing) {
            System.out.print("  " + key.label + ": ");
            System.out.flush();
            existing.put(key.name, scanner.nextLine().trim());
        }

        writeEnv(existing);
        DotEnv.reload();

        System.out.println();
        System.out.println("Configurazione salvata in .env — buon lavoro!");
        System.out.println();
    }

    /**
     * Reads the current {@code .env} file into an ordered map so that already-set keys are
     * preserved when writing the updated file back.
     *
     * @return a mutable, insertion-ordered map of the existing key/value pairs
     */
    private static Map<String, String> readExistingEnv() {
        Map<String, String> map = new LinkedHashMap<>();
        Path path = Path.of(".env");
        if (!Files.isReadable(path)) {
            return map;
        }
        try {
            for (String line : Files.readAllLines(path)) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) {
                    continue;
                }
                int sep = t.indexOf('=');
                if (sep <= 0) {
                    continue;
                }
                map.put(t.substring(0, sep).trim(), t.substring(sep + 1).trim());
            }
        } catch (IOException e) {
            System.err.println("Attenzione: impossibile leggere .env esistente: " + e.getMessage());
        }
        return map;
    }

    /**
     * Writes all key/value pairs to {@code .env}, ordering the known {@link #KEYS} first and
     * appending any extra keys that were already present in the file afterwards.
     *
     * @param values the complete set of key/value pairs to write
     */
    private static void writeEnv(Map<String, String> values) {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Path.of(".env")))) {
            Set<String> written = new LinkedHashSet<>();
            for (EnvKey key : KEYS) {
                String val = values.get(key.name);
                if (val != null) {
                    pw.println(key.name + "=" + val);
                    written.add(key.name);
                }
            }
            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (!written.contains(entry.getKey())) {
                    pw.println(entry.getKey() + "=" + entry.getValue());
                }
            }
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio di .env: " + e.getMessage());
        }
    }

    private record EnvKey(String name, String label) {}
}
