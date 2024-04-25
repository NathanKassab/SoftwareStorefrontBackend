package me.bannock.capstone.backend.accounts.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class AccountUserDetails implements UserDetails {

    /**
     * Makes a new user details wrapper for the user account
     * @param user The user account to make the details for
     */
    public AccountUserDetails(AccountDTO user){
        this.user = user;
    }

    private final AccountDTO user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.user.getAuthorities();
    }

    @Override
    public String getPassword() {
        return this.user.getPassword();
    }

    @Override
    public String getUsername() {
        return this.user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !this.user.isExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.user.isLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !this.user.isPasswordExpired();
    }

    @Override
    public boolean isEnabled() {
        return !this.user.isDisabled();
    }

    @Override
    public String toString() {
        return "AccountUserDetails{" +
                "user=" + user +
                '}';
    }

}
