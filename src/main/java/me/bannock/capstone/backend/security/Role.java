package me.bannock.capstone.backend.security;

import java.util.Set;

public enum Role {

    ROLE_GUEST(
            Set.of(Privilege.PRIV_LOGIN)
    ),
    ROLE_USER(
            Set.of(Privilege.PRIV_LOGIN, Privilege.PRIV_LAUNCH_LOADER, Privilege.PRIV_VIEW_MAIN_APP_PANEL,
                    Privilege.PRIV_VIEW_OWN_ACCOUNT_INFORMATION)
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
