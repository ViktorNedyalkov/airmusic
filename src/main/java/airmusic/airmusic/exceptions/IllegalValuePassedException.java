package airmusic.airmusic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class IllegalValuePassedException extends RuntimeException{
    public IllegalValuePassedException(){}

    public IllegalValuePassedException(String msg){
        super(msg);
    }
}
