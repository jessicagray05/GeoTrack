package com.jessicagray.geotrack.service;

import com.jessicagray.geotrack.model.User;
import com.jessicagray.geotrack.repository.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String loginIdentifier)
            throws UsernameNotFoundException {

        String identifier = loginIdentifier.trim();

        /*
         * First try email.
         *
         * If there is no email match, try the old username field.
         * This means your existing GeoTrack accounts continue working.
         */
        User user = userRepository
                .findByEmailIgnoreCase(identifier)
                .or(() ->
                        userRepository.findByUsernameIgnoreCase(identifier)
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "GeoTrack account not found."
                        )
                );

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true,
                true,
                true,
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole()
                        )
                )
        );
    }
}