package com.strazzullo_marocco_sibilla_marin.app.session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Persists the session token to {@code ~/.theknife/session} so the client can restore its
 * authenticated state across restarts without asking the user to log in again.
 *
 * @version 1.0
 * @Author Marocco Stefano, 762192, VA
 */
public final class SessionStore {

    private static final Path TOKEN_FILE =
            Path.of(System.getProperty("user.home"), ".theknife", "session");

    private SessionStore() {}

    /**
     * Writes the token to disk, creating the parent directory if needed.
     *
     * @param token the session token to persist
     */
    public static void save(String token) {
        try {
            Files.createDirectories(TOKEN_FILE.getParent());
            Files.writeString(TOKEN_FILE, token);
        } catch (IOException ignored) {}
    }

    /**
     * Reads the token from disk.
     *
     * @return the stored token, or empty if the file does not exist or is blank
     */
    public static Optional<String> load() {
        try {
            if (Files.exists(TOKEN_FILE)) {
                String token = Files.readString(TOKEN_FILE).strip();
                if (!token.isBlank()) return Optional.of(token);
            }
        } catch (IOException ignored) {}
        return Optional.empty();
    }

    /**
     * Deletes the stored token from disk.
     */
    public static void clear() {
        try {
            Files.deleteIfExists(TOKEN_FILE);
        } catch (IOException ignored) {}
    }
}
