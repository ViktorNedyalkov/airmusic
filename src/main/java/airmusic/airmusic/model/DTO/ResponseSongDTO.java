package airmusic.airmusic.model.DTO;

import airmusic.airmusic.model.POJO.Song;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Setter
@Getter
public class ResponseSongDTO {

    private long id;
    private ResponseUserDTO uploader;
    private String description;
    private String genre;
    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date uploadDate;
    private String title;
    private String trackUrl;
    private String amazonUrl;

    public ResponseSongDTO(Song song){
        this.id = song.getId();
        this.uploader = new ResponseUserDTO(song.getUploader());
        this.description = song.getDescription();
        this.genre = song.getGenre().getName();
        this.uploadDate = song.getUploadDate();
        this.title = song.getTitle();
        this.trackUrl = song.getTrackUrl();
        this.amazonUrl = song.getAmazonUrl();
    }

    public static List<ResponseSongDTO> respondSongs(List<Song> songs){
        List<ResponseSongDTO> responseSongs = new ArrayList<>();
        for (Song song : songs) {
            responseSongs.add(new ResponseSongDTO(song));
        }
        return responseSongs;
    }
}
