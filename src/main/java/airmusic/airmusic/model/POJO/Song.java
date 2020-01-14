package airmusic.airmusic.model.POJO;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tracks")
public class Song {

    @Id
    @GeneratedValue
    private long id;
    @ManyToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;
    private String description;
    @ManyToOne
    @JoinColumn(name = "genre_id")
    private Genre genre;
    private String title;
    @JsonFormat(pattern = "yyyy-mm-dd")
    private Date uploadDate;//SQL DATE, maybe change later
    private String trackUrl;
    private String amazonUrl;
    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", uploader=" + uploader +
                ", description='" + description + '\'' +
                ", genre=" + genre +
                ", title='" + title + '\'' +
                ", uploadDate=" + uploadDate +
                ", trackUrl='" + trackUrl + '\'' +
                '}';
    }
}
