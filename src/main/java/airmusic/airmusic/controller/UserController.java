package airmusic.airmusic.controller;

import airmusic.airmusic.model.DAO.UserDao;
import airmusic.airmusic.model.DTO.LoginUserDTO;
import airmusic.airmusic.model.DTO.RegisterUserDTO;
import airmusic.airmusic.model.User;
import airmusic.airmusic.model.repositories.UserRepository;
import com.google.gson.JsonObject;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;

import java.sql.SQLException;
import java.util.List;


@RestController
public class UserController {
    private static final String PASSWORD_REGEX = "vasko";//"^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-_]).{8,}$ ";
    /*
    At least one upper case English letter, (?=.*?[A-Z])
At least one lower case English letter, (?=.*?[a-z])
At least one digit, (?=.*?[0-9])
At least one special character, (?=.*?[#?!@$%^&*-])
Minimum eight in length .{8,} (with the anchors)
     */
    private static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";

    @Autowired
    private JdbcTemplate jdbcTemplate;//TODO check this. Not sure we need it at all

    @Autowired
    private UserRepository repo;
    @PostMapping("/register")
    public String registerUser(@RequestBody RegisterUserDTO dto) throws IOException, SQLException {
        JsonObject resp = new JsonObject();
        if (!dto.getConfirmPassword().equals(dto.getPassword())){
            resp.addProperty("Status","400");
            resp.addProperty("msg","Passwords do not match");
            return resp.toString();
        }
        if (!dto.getPassword().matches(PASSWORD_REGEX)){
            resp.addProperty("Status","400");
            resp.addProperty("msg","Password must contains one upper letter, one lower letter, one digit, one special symbol form {#?!@$%^&*-_} and be at least 8 characters");
            return resp.toString();
        }
        if (!dto.getEmail().matches(EMAIL_REGEX)){
            resp.addProperty("Status","400");
            resp.addProperty("msg","Not a valid e-mail");
            return resp.toString();
        }
        if (dto.getEmail().isEmpty()
                ||dto.getPassword().isEmpty()
                ||dto.getConfirmPassword().isEmpty()
                ||dto.getFirstName().isEmpty()
                ||dto.getLastName().isEmpty()
                ||dto.getGender().isEmpty()
                ||dto.getBirthDate().isEmpty()){
            resp.addProperty("Status","400");
            resp.addProperty("msg","Fill all fields");
            return resp.toString();
        }
        if (UserDao.doesExist(dto.getEmail())){
            resp.addProperty("Status","400");
            resp.addProperty("msg","User already exist with this email");
            return resp.toString();
        }
        User user =dto.toUser();
        //users gender to be converted to gender_id

        repo.save(user);
       // UserDao.addUserToDB(dto.toUser());
        resp.addProperty("Status","200");
        resp.addProperty("msg","User is registered");
        return resp.toString();
    }//// TODO: 6.1.2020 г. send mail
    @PostMapping("/login")//ok
    public String loginUser(@RequestBody LoginUserDTO dto,HttpSession session) throws IOException {
        JsonObject resp = new JsonObject();
        String email = dto.getEmail();
        String pass =dto.getPassword();
        User user = repo.findByEmail(email);
        if (user ==null || !user.getPassword().equals(pass)){
            resp.addProperty("status", "401");
            resp.addProperty("msg", "wrong email or password");
            return resp.toString();
        }
        session.setAttribute("logged",user);

        return "Successfully login";
    }
    @PostMapping("/users/follow/{id}")
    public String followUser(HttpSession session,@PathVariable("id") long id) throws SQLException {
        User user = (User) session.getAttribute("logged");
        if(user == null){
            return "Please, login";
            //throw exception
        }
        User followedUser = repo.findById(id);
        UserDao.followUser(user,followedUser);
        return user.toString();
    }

    //GET MAPPINGS
    @GetMapping("/getItAll")
    public List<User> getAll(){
        return repo.findAll();
    }

    //not mappings
    private User loggedUser(HttpSession session){
        User user= (User) session.getAttribute("logged");
        return user;
    }

    //Exception handler
    @ResponseStatus(value = HttpStatus.BAD_REQUEST,reason = "user already followed")
    @ExceptionHandler({SQLException.class})
    public void  handleException(){

    }
}
