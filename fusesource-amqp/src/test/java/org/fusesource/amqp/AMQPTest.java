package org.fusesource.amqp;

import org.junit.Test;

import java.net.URI;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPTest {

    @Test
    public void testBasic() throws Exception {

        AMQPSenderOptions producerOptions = new AMQPSenderOptions();
        final AMQPSender sender = AMQP.createSender(producerOptions);

        AMQPReceiverOptions consumerOptions = new AMQPReceiverOptions();
        final AMQPReceiver receiver = AMQP.createReceiver(consumerOptions);

        final AMQPClientOptions clientOptions = new AMQPClientOptions();
        clientOptions.setHost(new URI("tcp://localhost:61613"));
        AMQP.open(clientOptions, new Callback<AMQPConnection>() {
            public void onSuccess(final AMQPConnection connection) {
                AMQPSessionOptions sessionOptions = new AMQPSessionOptions(100, 1000);
                connection.createSession(sessionOptions, new Callback<AMQPSession>() {
                    public void onSuccess(final AMQPSession session) {
                        sender.attach(session, null);
                        receiver.attach(session, null);
                    }

                    public void onFailure(Throwable value) {
                        value.printStackTrace();
                    }
                });
            }
            public void onFailure(Throwable value) {
                value.printStackTrace();
            }
        });

    }

}
