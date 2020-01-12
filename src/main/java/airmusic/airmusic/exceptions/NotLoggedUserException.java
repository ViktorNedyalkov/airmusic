package airmusic.airmusic.exceptions;

public class NotLoggedUserException extends RuntimeException {
    public NotLoggedUserException(){}
    public NotLoggedUserException(String msg) {
        super(msg);
    }
}
