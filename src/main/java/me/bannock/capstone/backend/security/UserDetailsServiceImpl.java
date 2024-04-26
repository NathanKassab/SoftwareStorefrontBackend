package me.bannock.capstone.backend.security;

import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.AccountUserDetails;
import me.bannock.capstone.backend.accounts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    public UserDetailsServiceImpl(UserService userService){
        this.userService = userService;
    }

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AccountDTO> user = userService.getAccountWithEmail(username);
        if (user.isEmpty())
            throw new UsernameNotFoundException("No user with email \"%s\" in database".formatted(username));
        return new AccountUserDetails(user.get());
    }

}
