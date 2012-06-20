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

import org.fusesource.amqp.codec.types.ReceiverSettleMode;
import org.fusesource.amqp.codec.types.SenderSettleMode;
import org.fusesource.amqp.codec.types.Source;
import org.fusesource.amqp.codec.types.Target;

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
