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

package org.fusesource.amqp.callback;

import org.fusesource.amqp.AMQPConnectionOptions;
import org.fusesource.amqp.AMQPSessionOptions;
import org.fusesource.amqp.ExceptionListener;
import org.fusesource.amqp.types.Begin;
import org.fusesource.amqp.types.Open;
import org.fusesource.hawtdispatch.DispatchQueue;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPConnection {

    public static class Listener extends ExceptionListener {

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
        public AMQPServerSessionOptions onBegin(Begin begin) {
            return new AMQPServerSessionOptions();
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

    }

    public DispatchQueue queue();
    public AMQPConnectionOptions getOptions();
    public String remoteContainerId();
    public void close(String error, Callback<Void> callback);

    public AMQPSession createSession(AMQPSessionOptions sessionOptions, Callback<AMQPSession> cb);
    public AMQPSession[] sessions();

}
