package me.bannock.capstone.backend.security;

import java.util.Set;

public enum Role {

    ROLE_GUEST(
            Set.of(Privilege.PRIV_LOGIN)
    ),
    ROLE_USER(
            Set.of(Privilege.PRIV_LOGIN, Privilege.PRIV_USE_API,
                    Privilege.PRIV_LAUNCH_LOADER, Privilege.PRIV_VIEW_MAIN_APP_PANEL)
    ),
    ROLE_MODERATOR(
            Set.of(Privilege.PRIV_LOGIN, Privilege.PRIV_LAUNCH_LOADER,
                    Privilege.PRIV_VIEW_MAIN_APP_PANEL, Privilege.PRIV_VIEW_OWN_PRIVS,
                    Privilege.PRIV_VIEW_OTHER_USERS_PRIVS)
    ),
    ROLE_ADMINISTRATOR(
            Set.of(Privilege.PRIV_LOGIN, Privilege.PRIV_LAUNCH_LOADER,
                    Privilege.PRIV_VIEW_OWN_PRIVS, Privilege.PRIV_VIEW_OTHER_USERS_PRIVS,
                    Privilege.PRIV_MANAGE_OTHER_USERS_PRIVS, Privilege.PRIV_MANAGE_OWN_PRIVS)
    );

    Role(Set<Privilege> privileges){
        this.privileges = privileges;
    }

    private final Set<Privilege> privileges;

    public Set<Privilege> getPrivileges() {
        return privileges;
    }

}
