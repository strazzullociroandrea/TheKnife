package com.strazzullo_marocco_sibilla_marin.app.remote;

import strazzullo.User;

import java.io.Serial;
import java.io.Serializable;

/**
 * Carries both the authenticated {@link User} and the session token produced by a successful
 * login, so the client can store the token for future session restores without a second RMI call.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class LoginResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final User user;
    private final String sessionToken;

    /**
     * @param user         the authenticated user
     * @param sessionToken the UUID session token to persist on the client
     */
    public LoginResult(User user, String sessionToken) {
        this.user = user;
        this.sessionToken = sessionToken;
    }

    /**
     * @return the authenticated user
     */
    public User getUser() {
        return user;
    }

    /**
     * @return the session token
     */
    public String getSessionToken() {
        return sessionToken;
    }
}
