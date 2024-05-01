package me.bannock.capstone.backend.accounts.service;

import org.springframework.security.access.annotation.Secured;

import java.util.Optional;

public interface UserService {

    /**
     * Gets a user based on their email and password.
     * @param email The email on the account
     * @param password The user's unhashed password
     * @return The user's id
     * @throws UserServiceException If something goes wrong while logging in
     */
    long login(String email, String password) throws UserServiceException;

    /**
     * Logs into a user account using their api token
     * @param key The token sent alongside the request
     * @return The user's id
     * @throws UserServiceException If something goes wrong while logging into the account
     */
    long loginWithApiKey(String key) throws UserServiceException;

    /**
     * Registers a new account
     * @param email The new account's email
     * @param username The new account's username
     * @param password The new account's unhashed password
     * @throws UserServiceException If something goes wrong while creating the account
     */
    void register(String email, String username, String password) throws UserServiceException;

    /**
     * Generates a new api key for a user
     * @param uid The user's account's id to generate the api key for
     * @return The new api key
     * @throws UserServiceException If something goes wrong while setting the api key
     */
    String genApiKey(long uid) throws UserServiceException;

    /**
     * Gets an api key for a user
     * @param uid The user's account's id
     * @return The api key, if one could be found
     */
    Optional<String> getApiKey(long uid);

    /**
     * Gets a user account with their api key
     * @param apiKey The user's api key
     * @return The user's account's id
     */
    Optional<Long> getWithApiKey(String apiKey);

    /**
     * @param email The email to locate account with
     * @return The account's data, if it could be found
     */
    Optional<AccountDTO> getAccountWithEmail(String email);

    /**
     * @param uid The uid of the account you want to locate
     * @return The account's data, if it could be found
     */
    Optional<AccountDTO> getAccountWithUid(long uid);

    /**
     * Gets an account with their uid
     * @param uid The uid of the account
     * @return The account's email, if one could be found
     */
    Optional<String> getAccountEmail(long uid);

    /**
     * Grants a privilege to a user
     * @param uid The uid to grant the privilege to
     * @param privilegeName The name of the privilege
     * @throws UserServiceException If something goes wrong while granting the privilege
     */
    @Secured("PRIV_MANAGE_USER_PRIVS")
    void grantPrivilege(long uid, String privilegeName) throws UserServiceException;

    /**
     * Revokes a privilege from a user. Does nothing if the user doesn't already have the privilege
     * @param uid The uid to revoke the privilege from
     * @param privilegeName The name of the privilege to revoke
     * @throws UserServiceException If something goes wrong while revoking the privilege
     */
    @Secured("PRIV_MANAGE_USER_PRIVS")
    void revokePrivilege(long uid, String privilegeName) throws UserServiceException;

    /**
     * Used to tell whether a user is granted a privilege
     * @param uid The uid of the user
     * @param privilegeName The name of the privilege
     * @return True if the user has the privilege, otherwise false
     * @throws UserServiceException If something went wrong while checking fot the privilege
     */
    boolean hasPrivilege(long uid, String privilegeName) throws UserServiceException;

}
