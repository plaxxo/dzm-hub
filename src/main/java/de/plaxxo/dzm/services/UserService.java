package de.plaxxo.dzm.services;

import de.plaxxo.dzm.data.Role;
import de.plaxxo.dzm.data.User;
import de.plaxxo.dzm.data.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Erstellt einen neuen Benutzer
     */
    public User createUser(String username, String name, String password, Role... roles) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Benutzername existiert bereits: " + username);
        }

        User user = new User();
        user.setUsername(username);
        user.setName(name);
        user.setHashedPassword(passwordEncoder.encode(password));

        Set<Role> roleSet = new HashSet<>();
        for (Role role : roles) {
            roleSet.add(role);
        }
        user.setRoles(roleSet);

        return userRepository.save(user);
    }

    /**
     * Gibt alle Benutzer zurück
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Sucht einen Benutzer nach ID
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    /**
     * Sucht einen Benutzer nach Benutzernamen
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Aktualisiert einen Benutzer
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Ändert das Passwort eines Benutzers
     */
    public User changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Benutzer nicht gefunden: " + username);
        }

        user.setHashedPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    /**
     * Löscht einen Benutzer
     */
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }

}