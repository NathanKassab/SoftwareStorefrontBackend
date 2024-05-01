package me.bannock.capstone.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.bannock.capstone.backend.accounts.service.AccountDTO;
import me.bannock.capstone.backend.accounts.service.UserService;
import me.bannock.capstone.backend.accounts.service.UserServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

public class ApiAuthenticationFilter extends OncePerRequestFilter {

    public ApiAuthenticationFilter(UserService userService, UserDetailsService userDetailsService){
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    private final UserService userService;
    private final UserDetailsService userDetailsService;

    private final RequestMatcher apiMatcher = new AntPathRequestMatcher("/api/**");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // We don't bother if the requested isn't to the api
        if (!apiMatcher.matches(request)){
            filterChain.doFilter(request, response);
            return;
        }

        // We don't want to bother with requests that are already authenticated
        // because there's no need
        if (SecurityContextHolder.getContext().getAuthentication() != null
                && !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()){
            filterChain.doFilter(request, response);
            return;
        }

        // If this header is missing, the filter can't do anything. We ignore here
        if (request.getHeader("authorization") == null){
            filterChain.doFilter(request, response);
            return;
        }

        try{
            long uid = userService.loginWithApiKey(request.getHeader("authorization").split(" ")[1]);
            Optional<AccountDTO> account = userService.getAccountWithUid(uid);
            if (account.isEmpty()) { // Should never happen
                response.sendError(401, "API key belongs to user that doesn't exist");
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(account.get().getEmail());
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                    userDetails, userDetails.getPassword(), userDetails.getAuthorities()
            ));

            filterChain.doFilter(request, response);
        } catch (UserServiceException e) {
            response.sendError(401, "Unable to authenticate with header");
        }
    }

}
