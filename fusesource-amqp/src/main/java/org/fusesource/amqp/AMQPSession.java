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

import org.fusesource.amqp.codec.types.*;
import org.fusesource.amqp.codec.types.Error;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public interface AMQPSession {

    public static class Listener {

        /**
         * Peer is trying to attach an end point to the session.
         * @param attach
         */
        public void onAttach(Attach attach, Callback<AMQPEndpoint> callback) {
            callback.onFailure(new AMQPException("attach rejected"));
        }

        /**
         * The peer is gracefully closing the session.
         * @param error
         */
        public void onClose(Error error) {
        }

    }

    void close(Error error, Callback<Void> callback);

}
