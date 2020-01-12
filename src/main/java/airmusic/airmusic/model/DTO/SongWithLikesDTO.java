package airmusic.airmusic.model.DTO;

import airmusic.airmusic.model.POJO.Genre;
import airmusic.airmusic.model.POJO.Song;
import airmusic.airmusic.model.POJO.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
@Getter
@Setter
@NoArgsConstructor
public class SongWithLikesDTO {

    private long id;

    private User uploader;
    private String description;

    private Genre genre;
    private String title;
    private Date uploadDate;//SQL DATE, maybe change later
    private String trackUrl;
    private String amazonUrl;
    private int numberOfLikes;

    public static SongWithLikesDTO getFromSong(Song song, int numberOfLikes){
        SongWithLikesDTO toReturn = new SongWithLikesDTO();
        toReturn.setAmazonUrl(song.getAmazonUrl());
        toReturn.setDescription(song.getDescription());
        toReturn.setGenre(song.getGenre());
        toReturn.setId(song.getId());
        toReturn.setTrackUrl(song.getTrackUrl());
        toReturn.setTitle(song.getTitle());
        toReturn.setUploadDate(song.getUploadDate());
        toReturn.setUploader(song.getUploader());
        toReturn.setNumberOfLikes(numberOfLikes);

        return toReturn;
    }
}
