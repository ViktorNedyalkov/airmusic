package airmusic.airmusic.controller;

import airmusic.airmusic.model.DAO.UserDao;
import airmusic.airmusic.model.User;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;

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
    @PostMapping("/register")
    @ResponseBody
    public String registerUser(HttpServletRequest request) throws IOException, SQLException {
        JsonObject resp = new JsonObject();
        JsonObject object = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        System.out.println(object.toString());
        String email = object.get("email").getAsString();
        String password1 = object.get("password1").getAsString();
        String password2 = object.get("password2").getAsString();
        String firstName = object.get("firstName").getAsString();
        String lastName = object.get("lastName").getAsString();
        String gender = object.get("gender").getAsString();
        String birthDate = object.get("birthDate").getAsString();
        if (!password1.equals(password2)){
            resp.addProperty("Status","400");
            resp.addProperty("msg","Passwords do not match");
            return resp.toString();
        }
        if (!password1.matches(PASSWORD_REGEX)){
            resp.addProperty("Status","400");
            resp.addProperty("msg","Password must contains one upper letter, one lower letter, one digit, one special symbol form {#?!@$%^&*-_} and be at least 8 characters");
            return resp.toString();
        }
        if (!email.matches(EMAIL_REGEX)){
            resp.addProperty("Status","400");
            resp.addProperty("msg","Not a valid e-mail");
            return resp.toString();
        }
        if (email.isEmpty()
                ||password1.isEmpty()
                ||password2.isEmpty()
                ||firstName.isEmpty()
                ||lastName.isEmpty()
                ||gender.isEmpty()
                ||birthDate.isEmpty()){
            resp.addProperty("Status","400");
            resp.addProperty("msg","Fill all fields");
            return resp.toString();
        }
        if (UserDao.doesExist(email)){
            resp.addProperty("Status","400");
            resp.addProperty("msg","User already exist with this email");
            return resp.toString();
        }
        User user = new User();
        user.setBirthDate(birthDate);
        user.setLastName(lastName);
        user.setFirstName(firstName);
        user.setEmail(email);
        user.setGender(gender);
        UserDao.addUserToDB(user);
        resp.addProperty("Status","200");
        resp.addProperty("msg","User is registered");
        return resp.toString();
    }
}
