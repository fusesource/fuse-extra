package org.fusesource.amqp;

import org.fusesource.amqp.codec.types.Begin;
import org.fusesource.amqp.codec.types.Open;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPConnection {

    public static class Listener {

        /**
         * The peer has sent us an open request.  It invokes the callback
         * with the response open frame to complete opening the amqp connection.
         */
        public void onOpen(Open request, Open response, Callback<Open> callback) {
            callback.onSuccess(response);
        }

        /**
         * Peer is attempting op begin a new session on the connection.
         * This returns the SessionOptions that will be used to establish
         * the session..
         * 
         * @param begin
         * @return
         */
        public AMQPSessionOptions onBegin(Begin begin) {
            return new AMQPSessionOptions();
        }

        /**
         * A new remotely initiated session is now open.
         * @param session
         */
        public void onAccepted(AMQPSession session) {
            session.close(null, null);
        }

        /**
         * The peer is gracefully closing the connection.
         */
        public void onClose() {
        }

        /**
         * An error has been detected and the connection will be closed.
         * @param error
         */
        public void onException(Throwable error) {
        }

    }

    public AMQPConnectionOptions getOptions();
    public String remoteContainerId();
    public void close(String error, Callback<Void> callback);

    public AMQPSession createSession(AMQPSessionOptions sessionOptions, Callback<AMQPSession> cb);
    public AMQPSession[] sessions();

}
