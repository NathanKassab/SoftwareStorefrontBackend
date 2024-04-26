package me.bannock.capstone.backend.accounts.service.db;

import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.security.Privilege;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DaoUserServiceImplTest {

    @Autowired
    private DaoUserServiceImpl userService;

    private final String DEFAULT_TEST_PASSWORD = "test";

    @Test
    void getAccountDtoWithUid() throws Exception {
        Long uid = registerAndLogin();
        assertTrue(userService.getAccountWithUid(uid).isPresent());
    }

    @Test
    void registerAndLoginTest() throws Exception{
        registerAndLogin();
    }

    @Test
    void genApiKeyAndThenGetUserWithApiKey() throws Exception {
        Long userId = registerAndLogin();
        String apiKey = userService.genApiKey(userId);
        assertNotNull(apiKey);
        Optional<Long> user2 = userService.getWithApiKey(apiKey);
        assertTrue(user2.isPresent());
        assertEquals(userId, user2.get());

        // We also check that the api key returned matches what we have
        assertTrue(userService.getApiKey(-1).isEmpty());
        Optional<String> fetchedApiKey  = userService.getApiKey(userId);
        assertTrue(fetchedApiKey.isPresent());
        assertEquals(apiKey, fetchedApiKey.get());

        assertDoesNotThrow(() -> userService.loginWithApiKey(fetchedApiKey.get()));
    }

    @Test
    void failLoginInDifferentWays() throws Exception {
        Long userId = registerAndLogin();
        Optional<String> email = userService.getAccountEmail(userId);
        assertTrue(email.isPresent());
        assertThrows(Exception.class, () -> userService.login("", DEFAULT_TEST_PASSWORD));
        assertThrows(Exception.class, () -> userService.login(email.get(), ""));
    }

    @Test
    void getAccountWithEmail() throws Exception{
        // This account should never exist, so it will always be empty unless something is wrong
        assertTrue(userService.getAccountWithEmail("").isEmpty());

        // Now we try to fetch a real account
        Long userId = registerAndLogin();
        Optional<String> email = userService.getAccountEmail(userId);
        assertTrue(email.isPresent());
        Optional<AccountDTO> fetchedUserId = userService.getAccountWithEmail(email.get());
        assertTrue(fetchedUserId.isPresent());
        assertEquals(userId, fetchedUserId.get().getUid());
    }

    @Test
    void grantAndRevokePrivilegesTest() throws Exception {
        long userId = registerAndLogin();
        Optional<String> email = userService.getAccountEmail(userId);
        assertTrue(email.isPresent());

        userService.revokePrivilege(userId, Privilege.PRIV_LOGIN.getPrivilege());
        assertFalse(userService.hasPrivilege(userId, Privilege.PRIV_LOGIN.getPrivilege()));
        userService.grantPrivilege(userId, Privilege.PRIV_LOGIN.getPrivilege());
        assertTrue(userService.hasPrivilege(userId, Privilege.PRIV_LOGIN.getPrivilege()));
    }

    /**
     * Registers and logs into a new account using the DEFAULT_TEST_PASSWORD field
     * and a randomly generated email+username
     * @return The user id of the account
     * @throws Exception If something goes wrong while registering and logging into the account
     */
    private Long registerAndLogin() throws Exception {
        String email = "%s@localhost".formatted(Long.toString(System.currentTimeMillis(), Character.MAX_RADIX));
        String password = DEFAULT_TEST_PASSWORD;
        userService.register(email, email, password);
        Long id = userService.login(email, password);
        assertNotNull(id);
        return id;
    }

}