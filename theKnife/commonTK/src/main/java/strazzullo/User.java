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

    private String id, name, surname, email, passwordHash, domicile, dateOfBirth;

    public User(String id, String name, String surname, String email, String password, String domicile, String dateOfBirth, boolean isPasswordHashed) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.name = name;
        this.surname = surname;
        this.email = email;

        if(isPasswordHashed) {
            this.passwordHash = passwordHash;
        }else{
            this.passwordHash = User.hashPassword(password);
        }

        this.domicile = domicile;
        this.dateOfBirth = dateOfBirth;
    }

    public User(String id, String name, String surname, String email, String password, String domicile,  boolean isPasswordHashed) {
        this(id, name, surname, email, password, domicile, null, isPasswordHashed);
    }

    public User(String name, String surname, String email, String password, String domicile, boolean isPasswordHashed) {
        this(null, name, surname, email, password, domicile, null, isPasswordHashed);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setPassword(String password) {
        this.passwordHash = User.hashPassword(password);
    }

    public String getDomicile() {
        return domicile;
    }

    public void setDomicile(String domicile) {
        this.domicile = domicile;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public abstract String getRole();

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", surname=" + surname + ", email=" + email + ", domicile=" + domicile + ", dateOfBirth=" + dateOfBirth + "]";
    }

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