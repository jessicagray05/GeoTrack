package com.jessicagray.geotrack;

import com.jessicagray.geotrack.model.Organisation;
import com.jessicagray.geotrack.model.Project;
import com.jessicagray.geotrack.model.User;
import com.jessicagray.geotrack.repository.OrganisationRepository;
import com.jessicagray.geotrack.repository.ProjectRepository;
import com.jessicagray.geotrack.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@Profile("dev")
public class DataInitializer {

    @Bean
    CommandLineRunner initialiseGeoTrackData(
            UserRepository userRepository,
            OrganisationRepository organisationRepository,
            ProjectRepository projectRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            /*
             * ---------------------------------------------------------
             * HESI
             * ---------------------------------------------------------
             *
             * Development-only organisation and accounts.
             */

            Organisation hesi = organisationRepository
                    .findByNameIgnoreCase(
                            "Herts and Essex Site Investigations"
                    )
                    .orElseGet(() ->
                            organisationRepository.save(
                                    new Organisation(
                                            "Herts and Essex Site Investigations"
                                    )
                            )
                    );

            User admin = userRepository
                    .findByUsernameIgnoreCase("admin")
                    .orElse(null);

            if (admin == null) {

                admin = new User(
                        "admin",
                        passwordEncoder.encode("geotrack"),
                        "ADMIN"
                );

                admin.setOrganisation(hesi);

                userRepository.save(admin);
            }

            User staff = userRepository
                    .findByUsernameIgnoreCase("staff")
                    .orElse(null);

            if (staff == null) {

                staff = new User(
                        "staff",
                        passwordEncoder.encode("geotrack"),
                        "STAFF"
                );

                staff.setOrganisation(hesi);

                userRepository.save(staff);
            }

            /*
             * ---------------------------------------------------------
             * FAKE COMPANY B
             * ---------------------------------------------------------
             *
             * Development-only organisation used to test tenant
             * isolation.
             */

            Organisation northstar = organisationRepository
                    .findByNameIgnoreCase(
                            "Northstar Ground Engineering"
                    )
                    .orElseGet(() ->
                            organisationRepository.save(
                                    new Organisation(
                                            "Northstar Ground Engineering"
                                    )
                            )
                    );

            /*
             * ---------------------------------------------------------
             * NORTHSTAR TEST USER
             * ---------------------------------------------------------
             */

            User northstarUser = userRepository
                    .findByUsernameIgnoreCase(
                            "northstar.admin"
                    )
                    .orElse(null);

            if (northstarUser == null) {

                northstarUser = new User(
                        "northstar.admin",
                        passwordEncoder.encode(
                                "northstar-test"
                        ),
                        "ADMIN"
                );

                northstarUser.setOrganisation(northstar);

                userRepository.save(northstarUser);

                System.out.println(
                        "GeoTrack development test user created for Northstar."
                );
            }

            /*
             * ---------------------------------------------------------
             * NORTHSTAR TEST PROJECT
             * ---------------------------------------------------------
             */

            if (projectRepository
                    .findAllByOrganisationId(
                            northstar.getId()
                    )
                    .isEmpty()) {

                Project northstarProject = new Project();

                northstarProject.setProjectReference(
                        "NST-TEST-001"
                );

                northstarProject.setProjectName(
                        "Northstar Test Site"
                );

                northstarProject.setClientName(
                        "Northstar Test Client"
                );

                northstarProject.setLocation(
                        "Cambridge"
                );

                northstarProject.setDescription(
                        "Temporary project used to test "
                                + "GeoTrack organisation isolation."
                );

                northstarProject.setStatus(
                        "IN_PROGRESS"
                );

                northstarProject.setStartDate(
                        LocalDate.now()
                );

                northstarProject.setTargetCompletionDate(
                        LocalDate.now().plusDays(30)
                );

                northstarProject.setOrganisation(
                        northstar
                );

                projectRepository.save(
                        northstarProject
                );

                System.out.println(
                        "GeoTrack Northstar development test project created."
                );
            }

            System.out.println(
                    "GeoTrack development test data ready."
            );
        };
    }
}