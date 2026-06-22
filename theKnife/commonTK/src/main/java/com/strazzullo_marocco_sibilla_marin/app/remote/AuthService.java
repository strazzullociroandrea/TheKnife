package com.strazzullo_marocco_sibilla_marin.app.remote;
 
import strazzullo.User;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote service interface exposing authservice-specific actions via RMI. 
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA  
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public interface AuthService extends Remote {

     /**
     Function to login a user with the given email and password.
        @param email The email of the user.
        @param password The password of the user.
        @return The User object if the login is successful, null otherwise.
        @throws RemoteException If a remote communication error occurs.
      */
    User login(String email, String password) throws RemoteException;

    /**
    Function to register a new user with the given User object and password.
        @param u The User object containing user details.
        @param password The password for the new user.
        @throws RemoteException If a remote communication error occurs.
     */
    void register(User u, String password) throws RemoteException;

    /**
    Function to logout a user with the given userid.
        @param userid The ID of the user to logout.
        @throws RemoteException If a remote communication error occurs.
     */
    void logout(String userid)  throws RemoteException;

    /**
    Function to hash a password using a secure hashing algorithm.
        @param password The password to be hashed.
        @return The hashed password as a String.
        @throws Exception If it fails to hash the password.
     */
    String hashPassword(String password) throws Exception;

    /**
    Function to validate an email address format.
        @param email The email address to be validated.
        @return true if the email format is valid, false otherwise.
        @throws RemoteException If a remote communication error occurs.
     */
    boolean validateEmail(String email) throws RemoteException;
}
