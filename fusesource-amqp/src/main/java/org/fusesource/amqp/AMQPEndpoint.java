package org.fusesource.amqp;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPEndpoint {

    public static class Listener {
        /**
         * The remote end closed the link.
         */
        public void onClosed(boolean senderClosed, org.fusesource.amqp.codec.types.Error error) {
        }

        /**
         * Called when a transfer occurs on the endpoint.
         */
        public void onTransfer() {
        }
    }

    AMQPSession getSession();
    void attach(AMQPSession session, Callback<Void> callback);
    void detach(boolean closed, String error, Callback<Void> callback);
}
