/**
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fusesource.amqp.internal;

import org.fusesource.amqp.AMQPReceiver;
import org.fusesource.amqp.AMQPReceiverOptions;
import org.fusesource.amqp.Callback;
import org.fusesource.amqp.codec.interfaces.DeliveryState;
import org.fusesource.amqp.codec.types.*;
import org.fusesource.hawtbuf.Buffer;

import java.util.LinkedList;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Receiver extends Endpoint implements AMQPReceiver {


    Delivery currentDelivery;
    final LinkedList<Delivery> deliveries = new LinkedList<Delivery>();
    long pendingCredits;

    public Receiver(AMQPReceiverOptions options) {
        super(options);
        linkCredit = options.credit;
    }

    @Override
    protected void processFlowFrame(Session source, Flow flow) {
        if( flow.getDeliveryCount()!=null ) {
            deliveryCount = flow.getDeliveryCount();
        }
        if( flow.getAvailable()!=null ) {
            deliveryCount = flow.getAvailable();
        }
    }

    protected void processTransferFrame(Session source, final Transfer transfer, Buffer payload) throws Exception {
        deliveryCount ++;
        available--;
        if( available < 0 ) {
            available = 0;
        }
        linkCredit --;

        if( currentDelivery == null) {
            currentDelivery = new Delivery() {
                public void ack() {
                    final Accepted state = new Accepted();
                    sendDispositions(state);
                }

                public void nack() {
                    sendDispositions(new Rejected());
                }

                private void sendDispositions(final DeliveryState state) {
                    forechDisposition(new Callback<Disposition>() {
                        public void onSuccess(Disposition disposition) {
                            disposition.setRole(Role.RECEIVER.getValue());
                            if( options.receiverSettleMode == ReceiverSettleMode.FIRST ) {
                                disposition.setSettled(true);
                                disposition.setState(state);
                            }
                            current().sendDisposition(disposition);
                        }
                    });
                }
            };
        }
        currentDelivery.append(transfer, payload);
        deliveries.add(currentDelivery);
        if( !transfer.getMore() ) {
            currentDelivery = null;
        }
        options.listener.onTransfer();
    }

    public void addCredit(int value) {
        pendingCredits += value;
        checkFlowSendNeeded();
    }

    public Delivery poll() {
        Delivery delivery = deliveries.poll();
        if( delivery!=null ) {
            pendingCredits++;
            checkFlowSendNeeded();
        }
        return delivery;
    }

    public Delivery peek() {
        return deliveries.peek();
    }

    private void checkFlowSendNeeded() {
        if( linkCredit < pendingCredits ) {
            linkCredit += pendingCredits;
            pendingCredits = 0;
            current().sendFlow(); // Update the peer /w more credits..
        }
    }
}
