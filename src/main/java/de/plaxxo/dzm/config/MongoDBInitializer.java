package de.plaxxo.dzm.config;

import de.plaxxo.dzm.data.Role;
import de.plaxxo.dzm.data.User;
import de.plaxxo.dzm.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class MongoDBInitializer {

    @Bean
    public CommandLineRunner initDatabase(MongoRepository<User, String> userRepository,
                                          PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User defaultUser = new User();
                defaultUser.setUsername("ohahn");
                defaultUser.setName("Oliver Hahn");
                defaultUser.setHashedPassword(passwordEncoder.encode("password"));

                Set<Role> roles = new HashSet<>();
                roles.add(Role.ADMIN);
                defaultUser.setRoles(roles);

                userRepository.save(defaultUser);
            }
        };
    }
}