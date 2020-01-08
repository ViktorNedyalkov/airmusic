package airmusic.airmusic.model.DTO;


import airmusic.airmusic.model.POJO.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    private String birthDate;

    public User toUser(){
        User user = new User();
        user.setPassword(this.password);
        user.setGender(this.gender);
        user.setEmail(this.email);
        user.setFirstName(this.firstName);
        user.setLastName(this.lastName);
        user.setBirthDate(this.birthDate);
        return user;
    }

}
