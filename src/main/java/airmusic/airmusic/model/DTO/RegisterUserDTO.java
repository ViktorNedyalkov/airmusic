package airmusic.airmusic.model.DTO;


import airmusic.airmusic.model.POJO.Gender;
import airmusic.airmusic.model.POJO.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class RegisterUserDTO {
    private String email;
    private String password;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate birthDate;

    public User toUser(){
        User user = new User();
        user.setPassword(this.password);
        user.setEmail(this.email);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setBirthDate(this.birthDate);
        return user;
    }

}
