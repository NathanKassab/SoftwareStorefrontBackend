package me.bannock.capstone.backend.security;

public enum Privilege {

    PRIV_LOGIN("PRIV_LOGIN"),
    PRIV_VIEW_MAIN_APP_PANEL("PRIV_VIEW_MAIN_APP_PANEL"),
    PRIV_VIEW_ADMIN_PANEL("PRIV_VIEW_ADMIN_PANEL"),
    PRIV_OPEN_PURCHASE_URL("PRIV_OPEN_PURCHASE_URL"),
    PRIV_LAUNCH_LOADER("PRIV_LAUNCH_LOADER"),
    PRIV_MANAGE_OTHER_USERS_PRIVS("PRIV_MANAGE_OWN_PRIVS"),
    PRIV_MANAGE_OWN_PRIVS("PRIV_MANAGE_OWN_PRIVS"),
    PRIV_VIEW_OTHER_USERS_PRIVS("PRIV_VIEW_OTHER_USERS_PRIVS"),
    PRIV_VIEW_OWN_PRIVS("PRIV_VIEW_OWN_PRIVS"),
    PRIV_REGISTER_PRODUCT("PRIV_REGISTER_PRODUCT"),
    PRIV_MODIFY_PRODUCT_DETAILS("PRIV_MODIFY_PRODUCT_DETAILS"),
    PRIV_VIEW_OWN_PRODUCTS("PRIV_VIEW_OWN_PRODUCTS"),
    PRIV_ACTIVATE_LICENSE("PRIV_ACTIVATE_LICENSE"),
    PRIV_GET_LICENSES("PRIV_GET_LICENSES"),
    PRIV_USE_OWN_LICENSES("PRIV_USE_OWN_LICENSES"),
    PRIV_DEACTIVATE_LICENSE("PRIV_DEACTIVATE_LICENSE"),
    PRIV_DELETE_LICENSE("PRIV_DELETE_LICENSE");

    /**
     * @param privilege The privilege
     */
    Privilege(String privilege){
        this.privilege = privilege;
    }

    private final String privilege;

    public String getPrivilege() {
        return privilege;
    }
}