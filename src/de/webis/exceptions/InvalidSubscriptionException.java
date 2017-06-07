package de.webis.exceptions;

/**
 * Exception thrown for invalid subscription keys for the Microsoft APIs put in
 * <code>/resources/subscription.properties</code>
 */
public class InvalidSubscriptionException extends RuntimeException {
    /**
     * Class constructor specifying the exception message
     * @param msg exception message
     */
    public InvalidSubscriptionException(String msg){
        super(msg);
    }
}
