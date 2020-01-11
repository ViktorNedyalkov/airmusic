package airmusic.airmusic.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(){}
    public UserNotFoundException(String msg){
        super(msg);
    }
}
