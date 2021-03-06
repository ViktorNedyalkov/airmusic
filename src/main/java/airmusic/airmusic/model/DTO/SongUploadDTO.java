package airmusic.airmusic.model.DTO;


import airmusic.airmusic.model.POJO.Genre;
import airmusic.airmusic.model.POJO.Song;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SongUploadDTO {


    private String description;
    private long genre_id;
    private String title;

    public Song toSong(){
        Song song = new Song();
        song.setDescription(description);
        Genre genre = new Genre();
        genre.setId(genre_id);
        song.setTitle(title);

        return song;
    }
}
