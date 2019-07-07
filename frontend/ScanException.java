package frontend;

/**
 *
 * @author markus
 */
class ScanException extends RuntimeException {

    private final Character expected;

    public ScanException(String message) {
        super(message);
        this.expected = null;
    }

    public ScanException(String message, Character expected) {
        super(message);
        this.expected = expected;
    }

    public Character getExpected() {
        return expected;
    }

    @Override
    public String toString() {
        if (expected == null) {
            return "ScanException: " + getMessage();
        } else {
            return "ScanException: " + getMessage() 
                    + ", '" + expected + "' expected.";
        }
    }

}
