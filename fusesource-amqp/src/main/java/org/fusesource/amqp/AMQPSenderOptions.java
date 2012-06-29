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

import org.fusesource.amqp.types.MessageAnnotations;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPSenderOptions extends AMQPEndpointOptions {

    Boolean durable;
    Long ttl;
    Short priority;
    MessageAnnotations messageAnnotations;

    public AMQPSenderOptions() {}
    public AMQPSenderOptions(AMQPSenderOptions other) {
        super(other);
        this.durable = other.durable;
        this.ttl = other.ttl;
        this.priority = other.priority;
        this.messageAnnotations = messageAnnotations;
    }

    public AMQPSenderOptions copy() {
        return new AMQPSenderOptions(this);
    }

    public Boolean getDurable() {
        return durable;
    }

    public void setDurable(Boolean durable) {
        this.durable = durable;
    }

    public Long getTTL() {
        return ttl;
    }

    public void setTTL(Long ttl) {
        this.ttl = ttl;
    }

    public Short getPriority() {
        return priority;
    }

    public void setPriority(Short priority) {
        this.priority = priority;
    }

    public MessageAnnotations getMessageAnnotations() {
        return messageAnnotations;
    }
    public void setMessageAnnotations(MessageAnnotations messageAnnotations) {
        this.messageAnnotations = messageAnnotations;
    }
}
