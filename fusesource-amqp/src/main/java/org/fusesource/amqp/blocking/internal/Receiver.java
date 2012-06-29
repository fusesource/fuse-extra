package org.fusesource.amqp.blocking.internal;

import org.fusesource.amqp.AMQPException;
import org.fusesource.amqp.callback.AMQPDelivery;
import org.fusesource.amqp.future.AMQPEndpoint;
import org.fusesource.amqp.future.AMQPReceiver;
import org.fusesource.amqp.types.Envelope;
import org.fusesource.hawtdispatch.Task;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Receiver extends Endpoint implements org.fusesource.amqp.blocking.AMQPReceiver {
    private final org.fusesource.amqp.future.internal.Receiver receiver;

    public Receiver(AMQPReceiver receiver) {
        this.receiver = (org.fusesource.amqp.future.internal.Receiver) receiver;
        this.receiver.receiver.onTransfer = new Task(){
            public void run() {
                onTransfer();
            }
        };
    }

    @Override
    protected org.fusesource.amqp.future.internal.Endpoint endpoint() {
        return receiver;
    }

    public void addCredit(int value) {
        receiver.addCredit(value);
    }

    public AMQPDelivery peek() {
        return receiver.peek();
    }

    public AMQPDelivery poll() {
        return receiver.poll();
    }

    synchronized public void onTransfer() {
        this.notify();
    }

    synchronized public Envelope receive() throws AMQPException {
        try {
            AMQPDelivery rc = poll();
            while( rc == null ) {
                this.wait();
                rc = poll();
            }
            return rc.getMessage();
        } catch (AMQPException e) {
            throw e;
        } catch (Exception e) {
            throw new AMQPException(e);
        }
    }

}
