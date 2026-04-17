package com.hotelbooking.service;

import com.hotelbooking.entity.User;
import com.hotelbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
        );
    }

    @Transactional
    public User registerClient(String login, String rawPassword) {
        if (userRepository.findByLogin(login).isPresent()) {
            throw new RuntimeException("Login already exists");
        }
        User client = new User();
        client.setLogin(login);
        client.setPassword(passwordEncoder.encode(rawPassword));
        client.setRole(User.Role.CLIENT);
        return userRepository.save(client);
    }

    public User findByLogin(String login) {
        return userRepository.findByLogin(login).orElse(null);
    }

    // Создание администратора при старте (если не существует)
    @Transactional
    public void ensureAdminExists(String adminLogin, String adminPassword) {
        if (userRepository.findByLogin(adminLogin).isEmpty()) {
            User admin = new User();
            admin.setLogin(adminLogin);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(User.Role.ADMIN);
            userRepository.save(admin);
        }
    }
}