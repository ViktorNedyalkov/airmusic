package airmusic.airmusic.model.repositories;

import airmusic.airmusic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findById(long i);

    User findByEmail(String email);
}
