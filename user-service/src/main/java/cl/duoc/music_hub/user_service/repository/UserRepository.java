package cl.duoc.music_hub.user_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.ListCrudRepository;

import cl.duoc.music_hub.user_service.model.User;

public interface UserRepository extends ListCrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByEmailContainingIgnoreCase(String email);
}
