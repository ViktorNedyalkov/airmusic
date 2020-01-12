package airmusic.airmusic.model.POJO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "playlists")
public class Playlist  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idplaylist")
    private long id;
    @Column(name = "title")
    private String title;
    @Column (name = "description")
    private String description;
    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;
}
