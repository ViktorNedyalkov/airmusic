package airmusic.airmusic.model.DTO;
import airmusic.airmusic.model.POJO.Playlist;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import lombok.Getter;
import lombok.Setter;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;







@Setter

@Getter

public class ResponsePlaylistDTO {
    @NotNull
    private long id;
    @NotNull
    private String title;
    private ResponseUserDTO creator;
    private List<ResponseSongDTO> songs;



    public ResponsePlaylistDTO(Playlist playlist) {
        this.setId(playlist.getId());
        this.setTitle(playlist.getTitle());
        this.setCreator(new ResponseUserDTO(playlist.getCreator()));
    }
    public ResponsePlaylistDTO(Playlist playlist, List<Song> songs) {
        this(playlist);
        this.setSongs(ResponseSongDTO.respondSongs(songs));
    }
    public static List<ResponsePlaylistDTO> responsePlaylists(List<Playlist> playlists){
        List<ResponsePlaylistDTO> response = new ArrayList<>();
        for (Playlist playlist:playlists) {
            response.add(new ResponsePlaylistDTO(playlist));
        }
        return response;
    }
}