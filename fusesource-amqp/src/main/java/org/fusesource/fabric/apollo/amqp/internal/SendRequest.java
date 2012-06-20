package org.fusesource.fabric.apollo.amqp.internal;

import org.fusesource.fabric.apollo.amqp.Callback;
import org.fusesource.fabric.apollo.amqp.codec.interfaces.AMQPFrame;

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
