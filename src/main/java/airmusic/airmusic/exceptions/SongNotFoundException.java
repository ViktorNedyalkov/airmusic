package airmusic.airmusic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


public class SongNotFoundException extends RuntimeException {
    public SongNotFoundException(){}
    public SongNotFoundException(String msg){
        super(msg);
    }
}
