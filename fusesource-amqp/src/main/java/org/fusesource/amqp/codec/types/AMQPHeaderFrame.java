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

package org.fusesource.amqp.codec.types;

import org.fusesource.amqp.codec.AMQPDefinitions;
import org.fusesource.amqp.codec.interfaces.AMQPFrame;
import org.fusesource.amqp.codec.marshaller.AMQPProtocolHeaderCodec;
import org.fusesource.hawtbuf.Buffer;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * <p>
 * </p>
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPHeaderFrame implements AMQPFrame {

    public static Buffer PROTOCOL_HEADER = init();

    private static Buffer init() {
        Buffer rc = new Buffer(AMQPProtocolHeaderCodec.INSTANCE.getFixedSize());
        try {
            AMQPProtocolHeaderCodec.INSTANCE.encode(new AMQPHeaderFrame(), new DataOutputStream(rc.out()));
        } catch (IOException e) {
            throw new RuntimeException("Error initializing static protocol header buffer : " + e.getMessage());
        }

        return rc;
    }


    public short protocolId;
    public short major;
    public short minor;
    public short revision;

    public AMQPHeaderFrame() {
        protocolId = AMQPDefinitions.PROTOCOL_ID;
        major = AMQPDefinitions.MAJOR;
        minor = AMQPDefinitions.MINOR;
        revision = AMQPDefinitions.REVISION;
    }

    public AMQPHeaderFrame(AMQPHeaderFrame value) {
        this.protocolId = value.protocolId;
        this.major = value.major;
        this.minor = value.minor;
        this.revision = value.revision;
    }

    public short getMajor() {
        return major;
    }

    public void setMajor(short major) {
        this.major = major;
    }

    public short getMinor() {
        return minor;
    }

    public void setMinor(short minor) {
        this.minor = minor;
    }

    public short getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(short protocolId) {
        this.protocolId = protocolId;
    }

    public short getRevision() {
        return revision;
    }

    public void setRevision(short revision) {
        this.revision = revision;
    }

    public String toString() {
        return String.format("[AMQPHeaderFrame, {id:%s, major:%s, minor:%s, revision:%s}]", protocolId, major, minor, revision);
    }


}
