package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.NotLoggedUserException;
import airmusic.airmusic.model.POJO.User;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpSession;

public abstract class AbstractController {

    public static final String USER_SESSION_LOGGED = "logged";



    @ResponseStatus(value = HttpStatus.UNAUTHORIZED,reason = "please login")
    @ExceptionHandler({NotLoggedUserException.class})
    public void loggedExceptionHandler(){

    }

    
    @SneakyThrows
    protected User validateUser(HttpSession session){
        User user = (User) session.getAttribute(USER_SESSION_LOGGED);
        if(user == null){
            throw new NotLoggedUserException();
        }
        return user;
    }


}
