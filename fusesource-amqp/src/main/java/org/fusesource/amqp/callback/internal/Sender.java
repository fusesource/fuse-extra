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

package org.fusesource.amqp.callback.internal;

import org.fusesource.amqp.AMQPEndpointOptions;
import org.fusesource.amqp.AMQPSenderOptions;
import org.fusesource.amqp.types.Envelope;
import org.fusesource.amqp.types.Message;
import org.fusesource.amqp.callback.AMQPSender;
import org.fusesource.amqp.callback.Callback;
import org.fusesource.amqp.types.*;
import org.fusesource.amqp.types.Flow;
import org.fusesource.amqp.types.Header;

/**
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Sender extends Endpoint implements AMQPSender {

    private final AMQPSenderOptions options;

    public Sender(AMQPSenderOptions options) {
        this.options = options.copy();
    }

    @Override
    protected AMQPEndpointOptions options() {
        return options;
    }

    @Override
    protected void processFlowFrame(Session source, Flow flow) {
        if( flow.getLinkCredit()!=null ) {
            linkCredit = flow.getLinkCredit();
            current().pumpOverflow();
            options.getListener().onTransfer();
        }
    }

    public void send(Message message, Callback<AMQPDeliveryState> callback) {
        send(annotate(message), callback);
    }

    private Envelope annotate(Message message) {
        Envelope rc = new Envelope();
        final Header header = new Header();
        header.setDurable(options.getDurable());
        header.setTTL(options.getTTL());
        header.setPriority(options.getPriority());
        rc.setHeader(header);
        rc.setMessageAnnotations(options.getMessageAnnotations());
        rc.setMessage(message);
        return rc;
    }

    public void send(Envelope message, Callback<AMQPDeliveryState> callback) {
        getSession().queue().assertExecuting();
        current().send(message, callback);
    }

    public boolean full() {
        Session session = (Session) getSession();
        if( session == null )
            return true;

        session.queue().assertExecuting();

        if( linkCredit <= 0 )
            return true;
        if( session.remoteIncomingWindow <= 0 )
            return true;

        return false;
    }

}
