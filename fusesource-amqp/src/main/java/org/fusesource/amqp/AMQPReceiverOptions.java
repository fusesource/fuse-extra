package org.fusesource.amqp;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPReceiverOptions extends AMQPEndpointOptions {

    public long credit = 50;
    public boolean noLocal;
    public String selector;

    public AMQPReceiverOptions() {}
    public AMQPReceiverOptions(AMQPReceiverOptions other) {
        super(other);
        this.credit = other.credit;
        this.noLocal = other.noLocal;
        this.selector = other.selector;
    }

    public AMQPReceiverOptions copy() {
        return new AMQPReceiverOptions(this);
    }

}
