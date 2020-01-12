package airmusic.airmusic.model.POJO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tracks", schema = "mydb")
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @OneToOne
    @JoinColumn(name = "uploader_id")
    private User uploader;
    private String description;
    private long genre_id;
    private String title;
    private Date uploadDate;//SQL DATE, maybe change later
    private String trackUrl;

}
