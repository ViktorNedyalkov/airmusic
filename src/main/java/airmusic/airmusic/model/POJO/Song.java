package airmusic.airmusic.model.POJO;

import airmusic.airmusic.model.POJO.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DynamicUpdate
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
    private Date upload_date;//SQL DATE, maybe change later
    private String track_url;
}
