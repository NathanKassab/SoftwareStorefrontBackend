package me.bannock.capstone.backend.security;

import me.bannock.capstone.backend.accounts.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class BackendAuthProvider implements AuthenticationProvider {

    @Autowired
    public BackendAuthProvider(UserService userService, UserDetailsService userDetailsService){
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    private final Logger logger = LogManager.getLogger();
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        long userId;
        try {
            userId = userService.login(authentication.getName(), authentication.getCredentials().toString());
        } catch (Exception e) {
            logger.info("Failed to authenticate user", e);
            throw new AuthenticationException(e.getMessage()) {};
        }

        // We need to get a user details object from the user details service so we can create an auth token
        Optional<String> userEmail = userService.getAccountEmail(userId);
        if (userEmail.isEmpty())
            throw new RuntimeException("Couldn't get email for user id");
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail.get());

        if (!userDetails.isEnabled()){
            logger.info("Denied user login due to account being disabled, userDetails={}", userDetails);
            throw new BadCredentialsException("account is disabled");
        }

        return new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
