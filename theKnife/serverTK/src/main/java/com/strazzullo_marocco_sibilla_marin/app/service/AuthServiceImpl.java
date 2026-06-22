package com.strazzullo_marocco_sibilla_marin.app.service;
import com.strazzullo_marocco_sibilla_marin.app.remote.AuthService;

import com.strazzullo_marocco_sibilla_marin.app.dao.UserDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.UserDAOImpl;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.List;
import strazzullo.*;
import java.util.UUID;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/**
 * Remote service interface exposing authservice-specific actions via RMI.
 * Extends {@link UnicastRemoteObject} to participate in RMI remote runtime communication.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {


    /**
    * Unique identifier for serialization to ensure that a loaded class corresponds
    * exactly to the serialized object.
    */
    private static final long serialVersionUID = 1L;

    /**
     * The user DAO instance.
    */
    private final UserDAO userDAO;

    /**
    * List to keep track of logged-in users id.
    */
    private final List<String> loggedInUsersId;

    /**
    * AuthServiceImpl constructor. Exports the remote object and initializes the DAO layer.
    *
    * @throws RemoteException if RMI export fails
    */
    public AuthServiceImpl() throws RemoteException {
        super();
        this.userDAO = new UserDAOImpl();
        this.loggedInUsersId = new ArrayList<>();
    }

    /**
    * Function to login a user with the given email and password.
    * @param email The email of the user.
    * @param password The password of the user.
    * @return The User object if the login is successful, null otherwise.
    * @throws RemoteException If a remote communication error occurs.
     */
    public User login(String email, String password) throws RemoteException {
        try {
            User u = this.userDAO.findByEmail(email);
            if (u == null) {
                return null;
            }

            String hashedPassword = this.hashPassword(password);
            if (u.getPasswordHash().equals(hashedPassword) && !this.loggedInUsersId.contains(u.getId())) {
                this.loggedInUsersId.add(u.getId());
                return u;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RemoteException("Error during login", e);
        }
    }

    /**
    * Function to logout a user with the given userid.
    * @param userId The ID of the user to logout.
    * @throws RemoteException If a remote communication error occurs.
     */
    public void logout(String userId) throws RemoteException{
        this.loggedInUsersId.remove(userId);
    }

    /**
    * Registers a new user with the given User object and password.
    * @param u The User object containing user details.
    * @param password The password for the new user.
    * @throws RemoteException If a remote communication error occurs or if the email is already registered or invalid format.
    */
    public void register(User u, String password) throws RemoteException {
        try {
            if(!this.validateEmail(u.getEmail())) {
                throw new RemoteException("Email already registered or invalid format");
            }

            String hashedPassword = this.hashPassword(password);
            u.setPasswordHash(hashedPassword);
            this.userDAO.save(u);
        } catch (Exception e) {
            throw new RemoteException("Error registering user", e);
        }
    }

    /**
    * Function to hash a password using SHA-256 algorithm.
    * @param password The password to be hashed.
    * @return The hashed password as a String.
    * @throws Exception If it fails to hash the password.
    */
    public String hashPassword(String password) throws Exception {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        }catch(Exception e){
            throw new Exception("Hashing error", e);
        }

    }


    /**
    Validate email implementation. Check  if the email is not already registered in the database.
    @param email The email address to be validated.
    @return true if the email format is valid and not already registered, false otherwise.
    @throws RemoteException If a remote communication error occurs.
     */
    public boolean  validateEmail(String email) throws RemoteException {


        try{
            User tmp = this.userDAO.findByEmail(email);
                if(tmp != null) {
                    return false;
                }
             return true;
        }catch (SQLException e) {
            throw new RemoteException("Database error during validation", e);
        }

    }
}