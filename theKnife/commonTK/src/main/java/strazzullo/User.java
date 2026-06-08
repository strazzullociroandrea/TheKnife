package strazzullo;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Represents a generic user in the system.
 * This is an abstract class that provides the common attributes and methods
 * for all user types, such as {@link Client} and {@link Manager}.
 *
 * @version 1.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public abstract class User implements Serializable {
    /**
     * Unique identifier for serialization to ensure that a loaded class corresponds
     * exactly to the serialized object.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * User personal information.
     */
    private String id, name, surname, email, passwordHash, domicile, dateOfBirth;

    /**
     * User constructor to create a user with all parameters, including the id and the date of birth.
     *
     * @param id
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     * @param dateOfBirth
     */
    public Utente(String id, String name, String surname, String email, String password, String domicile, String dateOfBirth) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.passwordHash = Utente.hashPassword(password);
        this.domicile = domicile;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * User constructor to create a user with all parameters, including the id, without date of birth
     *
     * @param id
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     */
    public Utente(String id, String name, String surname, String email, String password, String domicile) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.passwordHash = Utente.hashPassword(password);
        this.domicile = domicile;
    }

    /**
     * User constructor to create a user with all parameters, without the id, with date of birth. The id is generated randomly.
     *
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     * @param dateOfBirth
     */
    public Utente(String name, String surname, String email, String password, String domicile, String dateOfBirth) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.passwordHash = Utente.hashPassword(password);
        this.domicile = domicile;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * User constructor to create a user with all parameters, without the id, without date of birth. The id is generated randomly.
     *
     * @param name
     * @param surname
     * @param email
     * @param password
     * @param domicile
     */
    public Utente(String name, String surname, String email, String password, String domicile) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.passwordHash = Utente.hashPassword(password);
        this.domicile = domicile;
    }


    /**
     * Function to return the user id
     *
     * @return the user id
     */
    public String getId() {
        return id;
    }

    /**
     * Function to set the user id
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Function to get the user name
     *
     * @return the user name
     */
    public String getName() {
        return name;
    }

    /**
     * Function to set the user name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Function to get the user surname
     *
     * @return the user surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Function to set the user surname
     *
     * @param surname
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Function to get the user email
     *
     * @return the user email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Function to set the user email
     *
     * @param email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Function to get the user password, hashed
     *
     * @return the user password, hashed
     */
    public String getPasswordHash() {
        return passwordHash;
    }


    /**
     * Function to set the user password, hashed
     *
     * @param passwordHash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Function to set the user password, not hashed. It provides to hashed it.
     *
     * @param password
     */
    public void setPassword(String password) {
        this.passwordHash = Utente.hashPassword(password);
    }

    /**
     * Function to get the user domicile
     *
     * @return the user domicile
     */
    public String getDomicile() {
        return domicile;
    }


    /**
     * Function to set the user domicile
     *
     * @param domicile
     */
    public void setDomicile(String domicile) {
        this.domicile = domicile;
    }


    /**
     * Function to get the user date of birth
     *
     * @return the user date of birth
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Function to set the user date of birth
     *
     * @param dateOfBirth
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Function to get the user role
     *
     * @return
     */
    public abstract String getRuolo();

    /**
     * Function to see user details
     *
     * @return String user details
     */
    @Override
    public String toString() {
        return "Utente [id=" + id + ", nome=" + nome + ", surname=" + surname + ", email=" + email + ", domicile=" + domicile + ", dateOfBirth=" + dateOfBirth + "]";
    }


    /**
     * Static function to hash a string
     *
     * @param password String to hash
     * @return String hashed
     * @throws RuntimeException if the hashing algorithm is not found
     */
    public static String hashPassword(String password) throws RuntimeException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();

            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error", e);
        }
    }

}