package org.fusesource.fabric.apollo.amqp;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPException extends Exception {

    public AMQPException() {
    }

    public AMQPException(Throwable cause) {
        super(cause);
    }

    public AMQPException(String message) {
        super(message);
    }

    public AMQPException(String message, Throwable cause) {
        super(message, cause);
    }

}
