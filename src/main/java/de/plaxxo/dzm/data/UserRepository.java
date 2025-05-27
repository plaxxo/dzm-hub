package de.plaxxo.dzm.data;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface UserRepository extends MongoRepository<User, String> {
    
    User findByUsername(String username);
    
    // Benutzerdefinierte Abfragen mit der MongoDB-Abfragesyntax
    @Query("{ 'roles': { $in: ['ADMIN'] } }")
    List<User> findAllAdmins();
    
    // Benutzerdefinierte Projektion (nur bestimmte Felder)
    @Query(value = "{ }", fields = "{ 'username': 1, 'name': 1 }")
    List<User> findAllUserBasicInfo();
    
    boolean existsByUsername(String username);
}