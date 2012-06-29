package org.fusesource.amqp.future.internal;

import org.fusesource.amqp.Future;
import org.fusesource.amqp.types.*;
import org.fusesource.amqp.future.AMQPSender;
import org.fusesource.hawtdispatch.Task;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Sender extends Endpoint implements AMQPSender {
    private final org.fusesource.amqp.callback.internal.Sender sender;

    public Sender(org.fusesource.amqp.callback.AMQPSender sender) {
        this.sender = (org.fusesource.amqp.callback.internal.Sender) sender;
    }

    @Override
    protected org.fusesource.amqp.callback.internal.Endpoint endpoint() {
        return sender;
    }

    public boolean full() {
        final Promise<Boolean> promise = new Promise<Boolean>();
        sender.queue().execute(new Task() {
            public void run() {
                promise.onSuccess(sender.full());
            }
        });
        return promise.awaitNoException();
    }

    public Future<AMQPDeliveryState> send(final Envelope message) {
        final Promise<AMQPDeliveryState> promise = new Promise<AMQPDeliveryState>();
        sender.queue().execute(new Task() {
            public void run() {
                sender.send(message, promise);
            }
        });
        return promise;
    }

    public Future<AMQPDeliveryState> send(final Message message) {
        final Promise<AMQPDeliveryState> promise = new Promise<AMQPDeliveryState>();
        sender.queue().execute(new Task() {
            public void run() {
                sender.send(message, promise);
            }
        });
        return promise;
    }
}
