package vvhile.basic.theorem;

/**
 *
 * @author markus
 */
public class NoProofFoundException extends Exception {

    public NoProofFoundException() {
    }

    public NoProofFoundException(String message) {
        super(message);
    }

    public NoProofFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoProofFoundException(Throwable cause) {
        super(cause);
    }
    
}
