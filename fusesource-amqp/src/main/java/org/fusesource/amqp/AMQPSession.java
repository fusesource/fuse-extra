package org.fusesource.amqp;

import org.fusesource.amqp.codec.types.*;
import org.fusesource.amqp.codec.types.Error;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPSession {

    public static class Listener {

        /**
         * Peer is trying to attach an end point to the session.
         * @param attach
         */
        public void onAttach(Attach attach, Callback<AMQPEndpoint> callback) {
            callback.onFailure(new AMQPException("attach rejected"));
        }

        /**
         * The peer is gracefully closing the session.
         * @param error
         */
        public void onClose(Error error) {
        }

    }

    void close(Error error, Callback<Void> callback);

}
