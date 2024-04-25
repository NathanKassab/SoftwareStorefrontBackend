package me.bannock.capstone.backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

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
    @Autowired
    public DefaultSecurityFilterChain configureHttp(HttpSecurity security, AuthenticationFailureHandler authFailureHandler,
                                                    AccessDeniedHandler accessDeniedHandler) throws Exception {
        security.authorizeHttpRequests(authManagerRegistry -> {
            authManagerRegistry.requestMatchers(
                    "/", "/helloWorld", "/logout*", "/register"
            ).permitAll().anyRequest().authenticated();
        });

        // Configure login
        security.formLogin(loginConfigurer -> {
            loginConfigurer.loginProcessingUrl("/processLogin")
                    .defaultSuccessUrl("/app/main?loggedIn=true", true)
                    .failureHandler(authFailureHandler)
                    .usernameParameter("email")
                    .passwordParameter("password")
                    .loginPage("/login").permitAll();
        });

        // We assign a custom access denied handler so we could log the failed attempt
        security.exceptionHandling(configurer -> configurer.accessDeniedHandler(accessDeniedHandler));

        security.csrf(Customizer.withDefaults());

        return security.build();
    }

}
