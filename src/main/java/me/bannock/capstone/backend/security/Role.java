package me.bannock.capstone.backend.security;

import java.util.Set;

public enum Role {

    ROLE_GUEST(
            Set.of(Privilege.PRIV_LOGIN)
    ),
    ROLE_USER(
            Set.of(Privilege.PRIV_LOGIN, Privilege.PRIV_VIEW_MAIN_APP_PANEL,
                    Privilege.PRIV_VIEW_OWN_ACCOUNT_INFORMATION, Privilege.PRIV_VIEW_OWN_PRIVS,
                    Privilege.PRIV_ACTIVATE_LICENSE)
    ),
    ROLE_SHOPPER(
            Set.of(Privilege.PRIV_LAUNCH_LOADER, Privilege.PRIV_USE_API, Privilege.PRIV_GET_LICENSES)
    ),
    ROLE_MERCHANT(
            Set.of(Privilege.PRIV_CREATE_LICENSE, Privilege.PRIV_DEACTIVATE_LICENSE, Privilege.PRIV_DELETE_LICENSE,
                    Privilege.PRIV_MODIFY_PRODUCT_DETAILS, Privilege.PRIV_REGISTER_PRODUCT, Privilege.PRIV_USE_API,
                    Privilege.PRIV_VIEW_OWN_PRODUCTS)
    ),
    ROLE_MODERATOR(
            Set.of(Privilege.PRIV_LOGIN, Privilege.PRIV_LAUNCH_LOADER,
                    Privilege.PRIV_VIEW_MAIN_APP_PANEL, Privilege.PRIV_VIEW_USER_PRIVS)
    ),
    ROLE_ADMINISTRATOR(
            Set.of(Privilege.PRIV_LOGIN, Privilege.PRIV_LAUNCH_LOADER,
                    Privilege.PRIV_VIEW_USER_PRIVS, Privilege.PRIV_MANAGE_USER_PRIVS)
    );

    Role(Set<Privilege> privileges){
        this.privileges = privileges;
    }

    private final Set<Privilege> privileges;

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

}
