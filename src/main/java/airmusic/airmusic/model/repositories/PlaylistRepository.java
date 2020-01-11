package airmusic.airmusic.model.repositories;

import airmusic.airmusic.model.POJO.Playlist;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaylistRepository extends JpaRepository<Playlist,Long> {

    List<Playlist> findAllByCreator_Id(long id);
    List<Playlist> findAllByTitleContaining(String title);
}
