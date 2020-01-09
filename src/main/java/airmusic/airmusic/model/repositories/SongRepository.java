package airmusic.airmusic.model.repositories;

import airmusic.airmusic.model.POJO.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    List<Song> findAllByUploaderId(long id);
    Song findById(long id);
}
