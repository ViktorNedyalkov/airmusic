package airmusic.airmusic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Illegal data passed")
public class ForbiddenSongActionException extends RuntimeException{
}
