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

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPEndpoint {

    public static class Listener {
        /**
         * The remote end closed the link.
         */
        public void onClosed(boolean senderClosed, org.fusesource.amqp.codec.types.Error error) {
        }

        /**
         * Called when a transfer occurs on the endpoint.
         */
        public void onTransfer() {
        }
    }

    AMQPSession getSession();
    void attach(AMQPSession session, Callback<Void> callback);
    void detach(boolean closed, String error, Callback<Void> callback);
}
