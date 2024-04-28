package me.bannock.capstone.backend.security;

import me.bannock.capstone.backend.accounts.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationFailureHandler authFailureHandler(){
        return new AuthFailureHandler();
    }

    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(64000);
        return loggingFilter;
    }

    @Bean
    @Autowired
    public DefaultSecurityFilterChain configureHttp(HttpSecurity security, AuthenticationFailureHandler authFailureHandler,
                                                    AccessDeniedHandler accessDeniedHandler,
                                                    UserService userService, UserDetailsService userDetailsService) throws Exception {
        security.authorizeHttpRequests(authManagerRegistry -> authManagerRegistry.requestMatchers(
                "/", "/helloWorld", "/logout*", "/register", "/api/licensing/1/generate/**"
        ).permitAll().anyRequest().authenticated());

        // Configure login
        security.formLogin(loginConfigurer -> loginConfigurer.loginProcessingUrl("/processLogin")
                .defaultSuccessUrl("/app/main?loggedIn=true", true)
                .failureHandler(authFailureHandler)
                .usernameParameter("email")
                .passwordParameter("password")
                .loginPage("/login").permitAll()
        );

        // We assign a custom access denied handler so we could log the failed attempt
        security.exceptionHandling(configurer -> configurer.accessDeniedHandler(accessDeniedHandler));

        security.csrf(Customizer.withDefaults());

        // This authenticates api users who are using the authentication header
        security.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .addFilterBefore(new ApiAuthenticationFilter(userService, userDetailsService), BasicAuthenticationFilter.class);

        return security.build();
    }

}
