package org.fusesource.fabric.apollo.amqp;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPSessionOptions {

    public long incomingWindow;
    public long outgoingWindow;
    public AMQPSession.Listener listener;

    public AMQPSessionOptions() {
        this(100, 100);
    }
    public AMQPSessionOptions(long incomingWindow, long outgoingWindow) {
        this(incomingWindow, outgoingWindow, new AMQPSession.Listener());
    }
    public AMQPSessionOptions(long incomingWindow, long outgoingWindow, AMQPSession.Listener listener) {
        this.incomingWindow = incomingWindow;
        this.listener = listener;
        this.outgoingWindow = outgoingWindow;
    }

    public AMQPSessionOptions(AMQPSessionOptions other) {
        this.incomingWindow = other.incomingWindow;
        this.listener = other.listener;
        this.outgoingWindow = other.outgoingWindow;
    }

    public AMQPSessionOptions copy() {
        return new AMQPSessionOptions(this);
    }

    public long getIncomingWindow() {
        return incomingWindow;
    }

    public void setIncomingWindow(long incomingWindow) {
        this.incomingWindow = incomingWindow;
    }

    public AMQPSession.Listener getListener() {
        return listener;
    }

    public void setListener(AMQPSession.Listener listener) {
        this.listener = listener;
    }

    public long getOutgoingWindow() {
        return outgoingWindow;
    }

    public void setOutgoingWindow(long outgoingWindow) {
        this.outgoingWindow = outgoingWindow;
    }
}
