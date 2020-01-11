package airmusic.airmusic.model.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordUserDTO {
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
}
