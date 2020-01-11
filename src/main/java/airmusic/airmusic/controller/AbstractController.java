package airmusic.airmusic.controller;

import airmusic.airmusic.exceptions.*;
import airmusic.airmusic.model.POJO.User;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartException;

import javax.servlet.http.HttpSession;

public abstract class AbstractController {

    public static final String USER_SESSION_LOGGED = "logged";



    @ResponseStatus(value = HttpStatus.UNAUTHORIZED,reason = "please login")
    @ExceptionHandler({NotLoggedUserException.class})
    public void loggedExceptionHandler(){

    }

    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Size of file is too large")
    @ExceptionHandler(MultipartException.class)
    public void uploadSizeExceeded() {

    }
    @SneakyThrows
    protected User validateUser(HttpSession session){
        User user = (User) session.getAttribute(USER_SESSION_LOGGED);
        if(user == null){
            throw new NotLoggedUserException();
        }
        return user;
    }
    //TODO optimization for BAD_REQUEST

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "user already exist with this email")
    @ExceptionHandler({UserAlreadyExistsException.class})
    public void userAlreadyExistHandler() {
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class})
    public String handleBadRequestException(BadRequestException e){
            return e.getMessage();
    }
}
