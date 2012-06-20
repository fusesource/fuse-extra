package org.fusesource.fabric.apollo.amqp;

import org.fusesource.fabric.apollo.amqp.codec.types.ReceiverSettleMode;
import org.fusesource.fabric.apollo.amqp.codec.types.SenderSettleMode;
import org.fusesource.fabric.apollo.amqp.codec.types.Source;
import org.fusesource.fabric.apollo.amqp.codec.types.Target;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPEndpointOptions {

    public String name;
    public Source source;
    public Target target;
    public long maxMessageSize = 10*1024*1024;
    public SenderSettleMode senderSettleMode = SenderSettleMode.SETTLED;
    public ReceiverSettleMode receiverSettleMode = ReceiverSettleMode.FIRST;

    public AMQPEndpoint.Listener listener = new AMQPEndpoint.Listener();

    public AMQPEndpointOptions() {}
    public AMQPEndpointOptions(AMQPEndpointOptions other) {
        this.name = other.name;
        this.source = other.source;
        this.target = other.target;
        this.senderSettleMode = other.senderSettleMode;
        this.receiverSettleMode = other.receiverSettleMode;
        this.listener = other.listener;
    }

    public AMQPEndpointOptions copy() {
        return new AMQPEndpointOptions(this);
    }

    public void setQoS(AMQPQoS qos) {
        switch(qos) {
            case AT_MOST_ONCE:
                senderSettleMode   = SenderSettleMode.SETTLED;
                receiverSettleMode = ReceiverSettleMode.FIRST;
                break;
            case AT_LEAST_ONCE:
                senderSettleMode = SenderSettleMode.MIXED;
                receiverSettleMode = ReceiverSettleMode.FIRST;
                break;
            case EXACTLY_ONCE:
                senderSettleMode = SenderSettleMode.MIXED;
                receiverSettleMode = ReceiverSettleMode.SECOND;
                break;
        }
    }

}
