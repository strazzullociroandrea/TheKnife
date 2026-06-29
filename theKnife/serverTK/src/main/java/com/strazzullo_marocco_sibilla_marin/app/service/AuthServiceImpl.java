package com.strazzullo_marocco_sibilla_marin.app.service;

import com.strazzullo_marocco_sibilla_marin.app.dao.SessionDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.UserDAO;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.SessionDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.dao.impl.UserDAOImpl;
import com.strazzullo_marocco_sibilla_marin.app.remote.AuthService;
import com.strazzullo_marocco_sibilla_marin.app.remote.LoginResult;
import strazzullo.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.UUID;

/**
 * RMI implementation of {@link AuthService}. Sessions are stored in the {@code session} DB table
 * so they survive server restarts and work across multiple concurrent client devices.
 *
 * @version 2.0
 * @Author Strazzullo Ciro Andrea, 763603, VA
 * @Author Marocco Stefano, 762192, VA
 * @Author Sibilla Ginevra, 761114, VA
 * @Author Marin Marco, 760622, VA
 */
public class AuthServiceImpl extends UnicastRemoteObject implements AuthService {

    private static final long serialVersionUID = 1L;

    private final UserDAO userDAO;
    private final SessionDAO sessionDAO;

    /**
     * Constructs the service and initialises the backing DAOs.
     *
     * @throws RemoteException if RMI export fails
     */
    public AuthServiceImpl() throws RemoteException {
        super();
        this.userDAO = new UserDAOImpl();
        this.sessionDAO = new SessionDAOImpl();
    }

    /**
     * {@inheritDoc}
     * Looks up the user by email, verifies the SHA-256 password hash, generates a UUID token,
     * persists the session row, and returns both the user and the token.
     */
    @Override
    public LoginResult login(String email, String password) throws RemoteException {
        try {
            User user = userDAO.findByEmail(email);
            if (user == null) return null;
            if (!user.getPasswordHash().equals(hashPassword(password))) return null;
            String token = UUID.randomUUID().toString();
            sessionDAO.save(token, user.getId());
            return new LoginResult(user, token);
        } catch (Exception e) {
            throw new RemoteException("Errore durante il login.", e);
        }
    }

    /**
     * {@inheritDoc}
     * Delegates to {@link SessionDAO#findUserByToken}, which joins the {@code session} and
     * {@code app_user} tables and rejects expired rows.
     */
    @Override
    public User validateSession(String token) throws RemoteException {
        try {
            return sessionDAO.findUserByToken(token);
        } catch (SQLException e) {
            throw new RemoteException("Errore durante la validazione della sessione.", e);
        }
    }

    /**
     * {@inheritDoc}
     * Deletes the session row for the given token so it can no longer be used to authenticate.
     */
    @Override
    public void logout(String token) throws RemoteException {
        try {
            sessionDAO.delete(token);
        } catch (SQLException e) {
            throw new RemoteException("Errore durante il logout.", e);
        }
    }

    /**
     * {@inheritDoc}
     * Checks email uniqueness, hashes the plain-text password before persisting, and delegates
     * the actual insert to {@link UserDAO#save}.
     */
    @Override
    public void register(User u, String password) throws RemoteException {
        try {
            if (!validateEmail(u.getEmail())) {
                throw new RemoteException("Esiste già un account con questa email.");
            }
            u.setPasswordHash(hashPassword(password));
            userDAO.save(u);
        } catch (SQLException e) {
            throw new RemoteException("Non è stato possibile registrarti. Riprova più tardi.");
        } catch (RemoteException e) {
            throw e;
        } catch (Exception e) {
            throw new RemoteException("Errore imprevisto. Riprova più tardi.");
        }
    }

    /**
     * {@inheritDoc}
     * Returns {@code true} when no row with the given email exists in the {@code app_user} table.
     */
    @Override
    public boolean validateEmail(String email) throws RemoteException {
        try {
            return userDAO.findByEmail(email) == null;
        } catch (SQLException e) {
            throw new RemoteException("Errore database durante la validazione dell'email.", e);
        }
    }

    /**
     * Computes the SHA-256 hex digest of the given plain-text password.
     *
     * @param password the plain-text password
     * @return the lowercase hex-encoded SHA-256 hash
     * @throws Exception if the SHA-256 algorithm is unavailable
     */
    private String hashPassword(String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
