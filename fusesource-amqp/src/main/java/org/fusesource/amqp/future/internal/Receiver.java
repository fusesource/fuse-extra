package org.fusesource.amqp.future.internal;

import org.fusesource.amqp.callback.AMQPDelivery;
import org.fusesource.amqp.future.AMQPReceiver;
import org.fusesource.hawtdispatch.Task;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Receiver extends Endpoint implements AMQPReceiver {
    public final org.fusesource.amqp.callback.internal.Receiver receiver;

    public Receiver(org.fusesource.amqp.callback.AMQPReceiver receiver) {
        this.receiver = (org.fusesource.amqp.callback.internal.Receiver) receiver;
    }

    @Override
    protected org.fusesource.amqp.callback.AMQPEndpoint endpoint() {
        return receiver;
    }

    public void addCredit(final int value) {
        receiver.queue().execute(new Task() {
            public void run() {
                receiver.addCredit(value);
            }
        });
    }

    public AMQPDelivery peek() {
        final Promise<AMQPDelivery> promise = new Promise<AMQPDelivery>();
        receiver.queue().execute(new Task() {
            public void run() {
                promise.onSuccess(receiver.peek());
            }
        });
        return promise.awaitNoException();
    }

    public AMQPDelivery poll() {
        final Promise<AMQPDelivery> promise = new Promise<AMQPDelivery>();
        receiver.queue().execute(new Task() {
            public void run() {
                promise.onSuccess(receiver.poll());
            }
        });
        return promise.awaitNoException();
    }

}
