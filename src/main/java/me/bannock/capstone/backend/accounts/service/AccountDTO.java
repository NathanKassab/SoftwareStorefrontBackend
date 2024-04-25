package me.bannock.capstone.backend.accounts.service;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AccountDTO {

    public AccountDTO(long uid, Collection<? extends GrantedAuthority> authorities,
                      String username, String password, boolean expired,
                      boolean passwordExpired, boolean locked, boolean disabled){
        this.uid = uid;
        this.authorities = authorities;
        this.password = password;
        this.username = username;
        this.expired = expired;
        this.passwordExpired = passwordExpired;
        this.locked = locked;
        this.disabled = disabled;
    }

    private final long uid;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String password;
    private final String username;
    private final boolean expired, passwordExpired, locked, disabled;

    public long getUid() {
        return uid;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isExpired() {
        return expired;
    }

    public boolean isPasswordExpired() {
        return passwordExpired;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isDisabled() {
        return disabled;
    }

    @Override
    public String toString() {
        return "AccountDTO{" +
                "uid=" + uid +
                ", authorities=" + authorities +
                ", password=***''" +
                ", username='" + username + '\'' +
                ", expired=" + expired +
                ", passwordExpired=" + passwordExpired +
                ", locked=" + locked +
                ", disabled=" + disabled +
                '}';
    }

}
