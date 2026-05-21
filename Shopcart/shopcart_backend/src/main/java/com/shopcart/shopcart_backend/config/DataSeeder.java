package com.shopcart.shopcart_backend.config;

import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shopcart.shopcart_backend.entities.*;
import com.shopcart.shopcart_backend.repositories.UserRepository;
import com.shopcart.shopcart_backend.entities.Role;

@Configuration
public class DataSeeder {

    @Value("${SUPER_ADMIN_PASSWORD}")
    private String superAdminPassword;

    @Bean
    CommandLineRunner seedSuperAdmin(
            UserRepository repo,
            PasswordEncoder encoder){

        return args -> {

            if(repo.findByEmail("super@gmail.com").isEmpty()){

                User user = new User();

                user.setName("Super Admin");
                user.setEmail("super@gmail.com");

                user.setPassword(
                        encoder.encode(superAdminPassword));

                user.setRole(Role.SUPER_ADMIN);

                repo.save(user);

                System.out.println(
                    "SUPER_ADMIN created.");
            }
        };
    }
}