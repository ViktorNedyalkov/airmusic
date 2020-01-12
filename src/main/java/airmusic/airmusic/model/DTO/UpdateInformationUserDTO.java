package airmusic.airmusic.model.DTO;

import airmusic.airmusic.model.POJO.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateInformationUserDTO {
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String birthDate;

    public User toUser(User user) {
        user.setEmail(this.email);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setBirthDate(this.birthDate);
        return user;
    }

}
