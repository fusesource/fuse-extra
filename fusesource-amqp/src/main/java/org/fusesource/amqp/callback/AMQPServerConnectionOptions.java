package org.fusesource.amqp.callback;

import org.fusesource.amqp.AMQPConnectionOptions;
import org.fusesource.amqp.AMQPSessionOptions;
import org.fusesource.hawtdispatch.transport.Transport;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPServerConnectionOptions extends AMQPConnectionOptions {

    public AMQPConnection.Listener listener = new AMQPConnection.Listener();
    private Transport transport;

    public AMQPServerConnectionOptions() {
    }

    public AMQPServerConnectionOptions(AMQPServerConnectionOptions other) {
        super(other);
        this.listener = other.listener;
        this.transport = other.transport;
    }

    @Override
    public AMQPServerConnectionOptions copy() {
        return new AMQPServerConnectionOptions(this);
    }

    public AMQPConnection.Listener getListener() {
        return listener;
    }

    public void setListener(AMQPConnection.Listener listener) {
        this.listener = listener;
    }

    public Transport getTransport() {
        return transport;
    }

    public void setTransport(Transport transport) {
        this.transport = transport;
    }

}
