/**
 * Copyright (C) 2012 FuseSource Corp. All rights reserved.
 * http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.amqp;

import org.fusesource.amqp.callback.AMQPEndpoint;
import org.fusesource.amqp.types.*;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPEndpointOptions {

    String name;
    Source source;
    Target target;
    long maxMessageSize = 10*1024*1024;
    SenderSettleMode senderSettleMode = SenderSettleMode.SETTLED;
    ReceiverSettleMode receiverSettleMode = ReceiverSettleMode.FIRST;

    private AMQPEndpoint.Listener listener = new AMQPEndpoint.Listener();

    public AMQPEndpointOptions() {}
    public AMQPEndpointOptions(AMQPEndpointOptions other) {
        this.setName(other.getName());
        this.setSource(other.getSource());
        this.setTarget(other.getTarget());
        this.setSenderSettleMode(other.getSenderSettleMode());
        this.setReceiverSettleMode(other.getReceiverSettleMode());
        this.setListener(other.getListener());
    }

    public AMQPEndpointOptions copy() {
        return new AMQPEndpointOptions(this);
    }

    public void setQoS(AMQPQoS qos) {
        switch(qos) {
            case AT_MOST_ONCE:
                setSenderSettleMode(SenderSettleMode.SETTLED);
                setReceiverSettleMode(ReceiverSettleMode.FIRST);
                break;
            case AT_LEAST_ONCE:
                setSenderSettleMode(SenderSettleMode.MIXED);
                setReceiverSettleMode(ReceiverSettleMode.FIRST);
                break;
            case EXACTLY_ONCE:
                setSenderSettleMode(SenderSettleMode.MIXED);
                setReceiverSettleMode(ReceiverSettleMode.SECOND);
                break;
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
    public void setSource(String source) {
        setSource(new Source(new AMQPString(source)));
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }
    public void setTarget(String target) {
        setTarget(new Target(new AMQPString(target)));
    }

    public long getMaxMessageSize() {
        return maxMessageSize;
    }

    public void setMaxMessageSize(long maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    public SenderSettleMode getSenderSettleMode() {
        return senderSettleMode;
    }

    public void setSenderSettleMode(SenderSettleMode senderSettleMode) {
        this.senderSettleMode = senderSettleMode;
    }

    public ReceiverSettleMode getReceiverSettleMode() {
        return receiverSettleMode;
    }

    public void setReceiverSettleMode(ReceiverSettleMode receiverSettleMode) {
        this.receiverSettleMode = receiverSettleMode;
    }

    public AMQPEndpoint.Listener getListener() {
        return listener;
    }

    public void setListener(AMQPEndpoint.Listener listener) {
        this.listener = listener;
    }
}
