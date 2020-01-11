package airmusic.airmusic.model.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class TrackToListDTO {
    private long playlist_id;
    private long song_id;
}
