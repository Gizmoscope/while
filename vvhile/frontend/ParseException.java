package vvhile.frontend;

/**
 * A parse exception is thrown whenever something goes wrong during the parsing
 * process. 
 * 
 * @author markus
 */
public class ParseException extends RuntimeException {

    /**
     * Creates a new parse exception.
     */
    public ParseException() {
        this("Parse Exception");
    }

    /**
     * Creates a new parse exception from a given message.
     * 
     * @param message a message
     */
    public ParseException(String message) {
        super(message);
    }
    
}
