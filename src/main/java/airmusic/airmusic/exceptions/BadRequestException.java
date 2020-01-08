package airmusic.airmusic.exceptions;

public class BadRequestException extends Exception {
    public BadRequestException(){}
    public BadRequestException(String msg) {
        super(msg);
    }

}
