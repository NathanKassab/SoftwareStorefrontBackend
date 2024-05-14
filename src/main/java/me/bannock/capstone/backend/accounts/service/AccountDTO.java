package me.bannock.capstone.backend.accounts.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class AccountDTO {

    public AccountDTO(long uid, Collection<? extends GrantedAuthority> authorities,
                      String email, String username,
                      String password, String apiKey, String hwid,
                      boolean expired, boolean passwordExpired,
                      boolean locked, boolean disabled){
        this.uid = uid;
        this.authorities = authorities;
        this.email = email;
        this.username = username;
        this.password = password;
        this.apiKey = apiKey;
        this.hwid = hwid;
        this.expired = expired;
        this.passwordExpired = passwordExpired;
        this.locked = locked;
        this.disabled = disabled;
    }

    private final long uid;

    @JsonIgnore
    private final Collection<? extends GrantedAuthority> authorities;

    private final String email;

    private final String username;

    @JsonIgnore
    private final String password;

    @JsonIgnore
    private final String apiKey;

    private final String hwid;

    private final boolean expired, passwordExpired, locked, disabled;

    public long getUid() {
        return uid;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getHwid() {
        return hwid;
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
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='***'" +
                ", apiKey='" + apiKey + '\'' +
                ", hwid='" + hwid + '\'' +
                ", expired=" + expired +
                ", passwordExpired=" + passwordExpired +
                ", locked=" + locked +
                ", disabled=" + disabled +
                '}';
    }

}
