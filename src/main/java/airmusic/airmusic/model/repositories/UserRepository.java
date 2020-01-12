package airmusic.airmusic.model.repositories;

import airmusic.airmusic.model.POJO.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByLastNameContaining(String lastName);
    List<User> findAllByFirstNameContaining(String firstName);
    User findByEmail(String email);
    List<User> findAll();
    boolean existsByEmail(String email);


}
