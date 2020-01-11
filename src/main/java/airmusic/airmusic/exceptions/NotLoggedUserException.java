package airmusic.airmusic.exceptions;

public class NotLoggedUserException extends Exception {
    public NotLoggedUserException(){}
    public NotLoggedUserException(String msg) {
        super(msg);
    }
}
