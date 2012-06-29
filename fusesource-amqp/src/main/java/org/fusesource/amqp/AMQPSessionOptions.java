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

import org.fusesource.amqp.callback.AMQPSession;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPSessionOptions {

    public long incomingWindow;
    public long outgoingWindow;

    public AMQPSessionOptions() {
        this(100, 100);
    }
    public AMQPSessionOptions(long incomingWindow, long outgoingWindow) {
        this.incomingWindow = incomingWindow;
        this.outgoingWindow = outgoingWindow;
    }

    public AMQPSessionOptions(AMQPSessionOptions other) {
        this.incomingWindow = other.incomingWindow;
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

    public long getOutgoingWindow() {
        return outgoingWindow;
    }

    public void setOutgoingWindow(long outgoingWindow) {
        this.outgoingWindow = outgoingWindow;
    }
}
