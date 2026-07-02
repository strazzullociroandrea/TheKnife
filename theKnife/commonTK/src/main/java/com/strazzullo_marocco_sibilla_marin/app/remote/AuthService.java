package com.strazzullo_marocco_sibilla_marin.app.remote;

import strazzullo.User;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote service interface exposing authentication actions via RMI.
 *
 * @version 3.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface AuthService extends Remote {

    /**
     * Authenticates the user and creates a persistent session.
     *
     * @param email    the user's email
     * @param password the plain-text password
     * @return a {@link LoginResult} containing the user and a session token, or {@code null} if
     *         the credentials are invalid
     * @throws RemoteException if a remote communication error occurs
     */
    LoginResult login(String email, String password) throws RemoteException;

    /**
     * Validates an existing session token and returns the associated user.
     *
     * @param token the session token previously returned by {@link #login}
     * @return the user associated with the token, or {@code null} if the token is expired or
     *         not found
     * @throws RemoteException if a remote communication error occurs
     */
    User validateSession(String token) throws RemoteException;

    /**
     * Invalidates the session identified by the given token.
     *
     * @param token the session token to invalidate
     * @throws RemoteException if a remote communication error occurs
     */
    void logout(String token) throws RemoteException;

    /**
     * Registers a new user.
     *
     * @param u        the user to register
     * @param password the plain-text password
     * @throws RemoteException if the email is already taken or a DB error occurs
     */
    void register(User u, String password) throws RemoteException;

    /**
     * Checks whether an email address is available (not already registered).
     *
     * @param email the email to check
     * @return {@code true} if the email is available, {@code false} if already taken
     * @throws RemoteException if a remote communication error occurs
     */
    boolean validateEmail(String email) throws RemoteException;

    /**
     * Updates the editable fields of a user's profile (name, surname, email, domicile, and date
     * of birth), leaving credentials and role untouched.
     *
     * @param userId the id of the user to update
     * @param name the new first name
     * @param surname the new last name
     * @param email the new email
     * @param domicile the new domicile/address, or null/blank to clear it
     * @param dateOfBirth the new date of birth, formatted "yyyy-MM-dd", or null/blank to clear it
     * @return the updated user
     * @throws RemoteException if the email is already taken by another user or a DB error occurs
     */
    User updateProfile(String userId, String name, String surname, String email, String domicile,
                        String dateOfBirth) throws RemoteException;
}
