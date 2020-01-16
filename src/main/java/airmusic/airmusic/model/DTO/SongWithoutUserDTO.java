package airmusic.airmusic.model.DTO;

import airmusic.airmusic.model.POJO.Genre;
import airmusic.airmusic.model.POJO.Song;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
public class SongWithoutUserDTO {
    private long id;
    private String description;
    private Genre genre;
    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date uploadDate;
    private String title;
    private String trackUrl;
    private String amazonUrl;

    public SongWithoutUserDTO(Song song){
        this.id = song.getId();
        this.description = song.getDescription();
        this.genre = song.getGenre();
        this.uploadDate = song.getUploadDate();
        this.title = song.getTitle();
        this.trackUrl = song.getTrackUrl();
        this.amazonUrl = song.getAmazonUrl();
    }
}
