package airmusic.airmusic.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "No such song exists")
public class SongNotFoundException extends RuntimeException {
}
