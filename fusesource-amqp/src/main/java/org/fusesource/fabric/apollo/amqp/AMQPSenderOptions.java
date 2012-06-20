package org.fusesource.fabric.apollo.amqp;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPSenderOptions extends AMQPEndpointOptions {

    public AMQPSenderOptions() {}
    public AMQPSenderOptions(AMQPSenderOptions other) {
        super(other);
    }

    public AMQPSenderOptions copy() {
        return new AMQPSenderOptions(this);
    }
}
