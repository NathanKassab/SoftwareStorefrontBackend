package me.bannock.capstone.backend.accounts.service;

import me.bannock.capstone.backend.accounts.service.db.DaoUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {

    @Bean
    @Autowired
    public UserService userService(DaoUserServiceImpl userService){
        return userService;
    }

}
