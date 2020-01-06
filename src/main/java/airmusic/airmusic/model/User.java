package airmusic.airmusic.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    private long id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    @Column(name = "gender_id")
    private String gender;
    private String birthDate;

    public User(long id, String email, String firstName, String lastName, String gender, String birthDate) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
    }

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
