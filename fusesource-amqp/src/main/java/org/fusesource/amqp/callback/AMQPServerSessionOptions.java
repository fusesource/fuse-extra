package org.fusesource.amqp.callback;

import org.fusesource.amqp.AMQPSessionOptions;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPServerSessionOptions extends AMQPSessionOptions {

    public AMQPSession.Listener listener = new AMQPSession.Listener();

    public AMQPServerSessionOptions() {
    }

    public AMQPServerSessionOptions(AMQPServerSessionOptions other) {
        super(other);
        this.listener = other.listener;
    }

    @Override
    public AMQPServerSessionOptions copy() {
        return new AMQPServerSessionOptions(this);
    }

    public AMQPSession.Listener getListener() {
        return listener;
    }

    public void setListener(AMQPSession.Listener listener) {
        this.listener = listener;
    }
}
