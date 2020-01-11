package airmusic.airmusic.model.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class EditPlaylistDTO {
    private long playlistId;
    private String description;
    private String title;
}
