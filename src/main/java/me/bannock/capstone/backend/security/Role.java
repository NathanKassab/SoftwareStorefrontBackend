package me.bannock.capstone.backend.security;

import java.util.Set;

public enum Role {

    ROLE_GUEST(
            Privilege.PRIV_LOGIN
    ),
    ROLE_USER(
            Privilege.PRIV_LOGIN, Privilege.PRIV_VIEW_MAIN_APP_PANEL, Privilege.PRIV_VIEW_OWN_ACCOUNT_INFORMATION,
            Privilege.PRIV_ACTIVATE_LICENSE, Privilege.PRIV_USE_OWN_LICENSES
    ),
    ROLE_SHOPPER(
            Privilege.PRIV_LAUNCH_LOADER, Privilege.PRIV_USE_API, Privilege.PRIV_GET_LICENSES
    ),
    ROLE_MERCHANT(
            Privilege.PRIV_CREATE_LICENSE, Privilege.PRIV_DEACTIVATE_LICENSE, Privilege.PRIV_DELETE_LICENSE,
            Privilege.PRIV_MODIFY_PRODUCT_DETAILS, Privilege.PRIV_REGISTER_PRODUCT, Privilege.PRIV_USE_API,
            Privilege.PRIV_VIEW_API_KEY, Privilege.PRIV_VIEW_OWN_PRODUCTS, Privilege.PRIV_BAN_OWN_PRODUCT_LICENSES,
            Privilege.PRIV_UNBAN_OWN_PRODUCT_LICENSES
    ),
    ROLE_MODERATOR(
            Privilege.PRIV_LAUNCH_LOADER, Privilege.PRIV_VIEW_MAIN_APP_PANEL,
            Privilege.PRIV_VIEW_USER_PRIVS, Privilege.PRIV_BAN_ANY_LICENSE, Privilege.PRIV_UNBAN_ANY_LICENSE,
            Privilege.PRIV_VIEW_API_KEY
    ),
    ROLE_ADMINISTRATOR(
            Privilege.PRIV_LAUNCH_LOADER, Privilege.PRIV_VIEW_USER_PRIVS,
            Privilege.PRIV_MANAGE_USER_PRIVS, Privilege.PRIV_BAN_ANY_LICENSE, Privilege.PRIV_UNBAN_ANY_LICENSE,
            Privilege.PRIV_VIEW_API_KEY
    );

    Role(Privilege... privileges){
        this(Set.of(privileges));
    }

    Role(Set<Privilege> privileges){
        this.privileges = privileges;
    }

    private final Set<Privilege> privileges;

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

}
