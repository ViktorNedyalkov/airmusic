package airmusic.airmusic.model.DAO;

import airmusic.airmusic.model.Song;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

@Component
public class SongDao {


    public List<Song> getAllSongs(){
        //todo get all songs
        return null;
    }

    public void addSong(Song song){
        //todo add the song
    }
}
