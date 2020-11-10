package vvhile.theorem;

/**
 * An instance of this class is thrown if the theorem prover is unable to find a
 * proof.
 * 
 * @author markus
 */
public class NoProofFoundException extends Exception {

    /**
     * Create a new NoProofFoundException without information.
     */
    public NoProofFoundException() {
    }

    /**
     * Create a new NoProofFoundException with some information.
     * 
     * @param message a message
     */
    public NoProofFoundException(String message) {
        super(message);
    }

    /**
     * Create a new NoProofFoundException with some information.
     * 
     * @param message a message
     * @param cause triggering cause
     */
    public NoProofFoundException(String message, Throwable cause) {
        super(message, cause);
    }


    /**
     * Create a new NoProofFoundException with some information.
     * 
     * @param cause triggering cause
     */
    public NoProofFoundException(Throwable cause) {
        super(cause);
    }
    
}
