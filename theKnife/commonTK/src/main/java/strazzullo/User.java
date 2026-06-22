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

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * the user data
     */
    private String id, name, surname, email, passwordHash, domicile, dateOfBirth;

    /**
     * Constructor to create a User instance with all attributes
     *
     * @param id               the user id
     * @param name             the user's name
     * @param surname          the user's surname
     * @param email            the user's email
     * @param password         the user's password
     * @param domicile         the user's domicile
     * @param dateOfBirth      the user's date of birth
     */
    public User(String id, String name, String surname, String email, String password, String domicile, String dateOfBirth) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.passwordHash = password;
        this.domicile = domicile;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Constructor to create a User instance with all attributes, without date of birth
     *
     * @param id               the user id
     * @param name             the user's name
     * @param surname          the user's surname
     * @param email            the user's email
     * @param password         the user's password
     * @param domicile         the user's domicile
     */
    public User(String id, String name, String surname, String email, String password, String domicile) {
        this(id, name, surname, email, password, domicile, null);
    }

    /**
     * Constructor to create a User instance with all attributes, without date of birth and id
     *
     * @param name             the user's name
     * @param surname          the user's surname
     * @param email            the user's email
     * @param password         the user's password
     * @param domicile         the user's domicile
     */
    public User(String name, String surname, String email, String password, String domicile) {
        this(null, name, surname, email, password, domicile, null);
    }

    /**
     * Function to get the user's id
     *
     * @return the user's id
     */
    public String getId() {
        return id;
    }

    /**
     * Function to set the user's id
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Function to get the user's name
     *
     * @return the user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Function to set the user's name
     *
     * @param name the user's name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Function to get the user's surname
     *
     * @return the user's surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * Function to get the user's surname
     *
     * @param surname the user's surname
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * Function to get the user's email
     *
     * @return the user's email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Function to set the user's email
     *
     * @param email the user's email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Function to get the user's hashed password
     *
     * @return the user's hasherd password
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Function to set the hashed password
     *
     * @param passwordHash the user's hashed password
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }


    /**
     * Function to get the user's domicile
     *
     * @return the user's domicile
     */
    public String getDomicile() {
        return domicile;
    }

    /**
     * Function to set the user's domicile
     *
     * @param domicile the user's domicile
     */
    public void setDomicile(String domicile) {
        this.domicile = domicile;
    }

    /**
     * Function to get the user's date of birth
     *
     * @return the user's date of birth as string
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Function to set the user's date of birth
     *
     * @param dateOfBirth the user's date of birth
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Abstract funtion to return the user's role
     *
     * @return the user's role
     */
    public abstract String getRole();


}