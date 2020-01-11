package airmusic.airmusic.model.DTO;
import airmusic.airmusic.model.POJO.Playlist;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import java.util.List;







@Setter

@Getter

public class ResponsePlaylistDTO {
    @NotNull
    private long id;
    @NotNull
    private String title;
    private User creator;
    private List<Song> songs;



    public ResponsePlaylistDTO(Playlist playlist) {
        this.setId(playlist.getId());
        this.setTitle(playlist.getTitle());
    }
    public ResponsePlaylistDTO(Playlist playlist, List<Song> songs) {
        this(playlist);
        this.setSongs(songs);
    }

}