package airmusic.airmusic.exceptions;



public class IllegalValuePassedException extends RuntimeException{
    public IllegalValuePassedException(){}

    public IllegalValuePassedException(String msg){
        super(msg);
    }
}
