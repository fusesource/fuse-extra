package org.fusesource.amqp.future;


import org.fusesource.amqp.*;
import org.fusesource.amqp.AMQPReceiverOptions;
import org.fusesource.amqp.AMQPSenderOptions;
import org.fusesource.amqp.callback.Callback;
import org.fusesource.amqp.future.internal.Connection;
import org.fusesource.amqp.future.internal.Receiver;
import org.fusesource.amqp.future.internal.Sender;
import org.fusesource.amqp.future.internal.Promise;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQP {

    static public Future<AMQPConnection> open(AMQPClientOptions o) {
        final Promise<AMQPConnection> promise = new Promise<AMQPConnection>();
        org.fusesource.amqp.callback.AMQP.open(o, new Callback<org.fusesource.amqp.callback.AMQPConnection>(){
            @Override
            public void onSuccess(org.fusesource.amqp.callback.AMQPConnection value) {
                promise.onSuccess(new Connection(value));
            }
            @Override
            public void onFailure(Throwable value) {
                promise.onFailure(value);
            }
        });
        return promise;        
    }

    static public AMQPReceiver createReceiver(AMQPReceiverOptions options) {
        return new Receiver(org.fusesource.amqp.callback.AMQP.createReceiver(options));
    }

    static public AMQPSender createSender(AMQPSenderOptions options) {
        return new Sender(org.fusesource.amqp.callback.AMQP.createSender(options));
    }

}
