package airmusic.airmusic.model.DTO;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Setter
@Getter
public class SongEditDTO {

    @NotNull
    @NotEmpty
    private String description;
}
