package airmusic.airmusic.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Setter
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

//    @JsonIgnore
//    @ManyToMany
//    @JoinTable(
//            name = "users_follow_users",
//            joinColumns = @JoinColumn(name = "follower_id"),
//            inverseJoinColumns = @JoinColumn(name = "followed_id"))
//    private List<User> followers;
//    @JsonIgnore
//    @ManyToMany
//    @JoinTable(
//            name = "users_follow_users",
//            joinColumns = @JoinColumn(name = "followed_id"),
//            inverseJoinColumns = @JoinColumn(name = "follower_id"))
//    private List<User> following;

    public User() {
        //followers = new ArrayList<>();
        //following = new ArrayList<>();
    }

    public User(long id, String email, String firstName, String lastName, String gender, String birthDate) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public void addToFollowers(User user){
       // this.followers.add(user);
    }
    public int setGenderID(String gender) {
        if (gender.equals("male")) {
            return 1;
        }
        if (gender.equals("female")) {
            return 2;
        }
        return 3;
    }

}
