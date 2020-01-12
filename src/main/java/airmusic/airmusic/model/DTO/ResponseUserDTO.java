package airmusic.airmusic.model.DTO;

import airmusic.airmusic.model.POJO.Gender;
import airmusic.airmusic.model.POJO.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class ResponseUserDTO {

    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private String birthDate;
    private String avatar;
    private boolean activated;

    public ResponseUserDTO(User user){
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.gender=user.getGender().getName();
        this.lastName =user.getLastName();
        this.birthDate = user.getBirthDate().toString();
        this.avatar = user.getAvatar();
        this.activated = user.isActivated();
    }
    public  static List<ResponseUserDTO> usersToRespond(List<User> users){
        List<ResponseUserDTO> rsp =new ArrayList<>();
        for (User user : users) {
            rsp.add(new ResponseUserDTO(user));
        }
        return rsp;
    }
}
