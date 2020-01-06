package airmusic.airmusic.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class Song {
    private long id;
    private long uploader_id;
    private String description;
    private int genre_id;
    private String title;
    private Date date;//could be changed to sql date
}
