package airmusic.airmusic.model.repositories;

import airmusic.airmusic.model.Song;
import airmusic.airmusic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    Song findById(long i);
    List<Song> findAllByUploaderId(long id);

}
