package me.bannock.capstone.backend.accounts.service.db;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
public class AccountModel {

    public AccountModel(){}

    protected AccountModel(String email, String username, String password){
        this.email = email;
        this.username = username;
        this.password = password;
        this.emailVerified = false;
        this.hwid = null;
        this.disabled = false;
    }

    @Id
    @Column(name = "uid", unique = true)
    @SequenceGenerator(name = "user_seq", sequenceName = "USER_SEQ", initialValue = 0, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "user_seq")
    private Long id;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "username", unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "hwid", length = 1024)
    private String hwid;

    @Column(name = "privileges")
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "uid"))
    private List<String> privileges;

    @Column(name = "disabled")
    private boolean disabled;

    public Long getId() {
        return id;
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

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getHwid() {
        return hwid;
    }

    public void setHwid(String hwid) {
        this.hwid = hwid;
    }

    public ArrayList<String> getPrivileges() {
        return new ArrayList<>(privileges);
    }

    public void setPrivileges(List<String> privileges) {
        this.privileges = privileges;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String toString() {
        return "AccountModel{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", password='***" + '\'' +
                ", emailVerified=" + emailVerified +
                ", apiKey='" + apiKey + '\'' +
                ", privileges=" + privileges +
                ", disabled=" + disabled +
                '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        AccountModel that = (AccountModel) object;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
