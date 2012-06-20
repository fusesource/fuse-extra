package org.fusesource.amqp.internal;

import org.fusesource.amqp.Callback;
import org.fusesource.amqp.codec.interfaces.AMQPFrame;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class SendRequest {
    public final AMQPFrame frame;
    public final Callback<Void> cb;
    public SendRequest(AMQPFrame frame, Callback<Void> cb) {
        this.frame = frame;
        this.cb = cb;
    }
}
