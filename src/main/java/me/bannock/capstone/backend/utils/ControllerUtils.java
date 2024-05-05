package me.bannock.capstone.backend.utils;

import me.bannock.capstone.backend.security.Privilege;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

public class ControllerUtils {

    /**
     * Checks if the currently logged-in user has all the privileges in the provided array
     * @param neededPrivs The provided array of privileges
     * @return True if the user has all the listed privileges
     */
    public static boolean hasPrivs(Privilege... neededPrivs){
        return Arrays.stream(neededPrivs).allMatch(priv -> {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(userPriv -> {
                return userPriv.getAuthority().equals(priv.getPrivilege());
            });
        });
    }

}
