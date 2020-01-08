package airmusic.airmusic.model.repositories;

import airmusic.airmusic.model.POJO.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findById(long i);
    User findByEmail(String email);
    List<User> findAll();
}
