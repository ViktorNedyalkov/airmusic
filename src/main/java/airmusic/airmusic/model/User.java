package airmusic.airmusic.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpClientErrorException.BadRequest;

@Getter
@Setter
@NoArgsConstructor
public class User {
    private long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String gender;
    private String BirthDate;
    public int setGenderID(String gender)  {
        if(gender.equals("male")){
            return 1;
        }
        if (gender.equals("female")){
            return 2;
        }
        return 3;
    }

}
