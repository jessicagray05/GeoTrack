package com.jessicagray.geotrack;

import com.jessicagray.geotrack.model.User;
import com.jessicagray.geotrack.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner createDefaultUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // Create or update ADMIN user

            User admin = userRepository
                    .findByUsername("admin")
                    .orElse(null);

            if (admin == null) {

                admin = new User(
                        "admin",
                        passwordEncoder.encode("geotrack"),
                        "ADMIN"
                );

                userRepository.save(admin);

                System.out.println(
                        "GeoTrack admin user created."
                );

            } else {

                admin.setPassword(
                        passwordEncoder.encode("geotrack")
                );

                admin.setRole("ADMIN");

                userRepository.save(admin);

                System.out.println(
                        "GeoTrack admin password reset."
                );
            }


            // Create STAFF user

            User staff = userRepository
                    .findByUsername("staff")
                    .orElse(null);

            if (staff == null) {

                staff = new User(
                        "staff",
                        passwordEncoder.encode("geotrack"),
                        "STAFF"
                );

                userRepository.save(staff);

                System.out.println(
                        "GeoTrack staff user created."
                );

            } else {

                staff.setPassword(
                        passwordEncoder.encode("geotrack")
                );

                staff.setRole("STAFF");

                userRepository.save(staff);

                System.out.println(
                        "GeoTrack staff password reset."
                );
            }

        };
    }
}