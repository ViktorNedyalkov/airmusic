package airmusic.airmusic.model.POJO;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.*;
import java.time.LocalDate;


@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String email;
    @JsonIgnore
    private String password;
    private String firstName;
    private String lastName;
    @ManyToOne
    @JoinColumn(name = "gender_id")
    private Gender gender;
    private LocalDate birthDate;
    private String avatar;
    private boolean activated;



    public User(long id, String email, String firstName, String lastName, LocalDate birthDate) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
    }

    public void setPassword(String password){
        this.password = BCrypt.hashpw(password,BCrypt.gensalt());
    }
}
