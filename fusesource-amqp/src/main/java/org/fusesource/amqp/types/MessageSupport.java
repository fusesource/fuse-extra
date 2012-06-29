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

package org.fusesource.amqp.types;

import org.fusesource.amqp.codec.marshaller.TypeReader;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.DataByteArrayInputStream;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.List;

/**
 *
 */
public class MessageSupport {

    public static Buffer toBuffer(Message message) throws Exception {
        if ( message == null ) {
            return null;
        }
        return message.toBuffer();
    }

    public static Buffer toBuffer(Envelope message) throws Exception {
        if ( message == null ) {
            return null;
        }
        return message.toBuffer();
    }

    public static long size(Message message) {
        if ( message == null ) {
            return 0;
        }
        return message.size();
    }

    public static long size(Envelope message) {
        if ( message == null ) {
            return 0;
        }
        return message.size();
    }

    public static void write(Envelope message, DataOutput out) throws Exception {
        if ( message == null ) {
            return;
        }
        message.write(out);
    }

    public static void write(Message message, DataOutput out) throws Exception {
        if ( message == null ) {
            return;
        }
        message.write(out);
    }


    public static Message message(Object... args) {
        Message rc = new Message();
        for ( Object arg : args ) {
            if ( arg instanceof Properties ) {
                rc.setProperties((Properties) arg);
            } else if ( arg instanceof ApplicationProperties ) {
                rc.setApplicationProperties((ApplicationProperties) arg);
            } else if ( arg instanceof Data ) {
                rc.setData((Data)arg);
            } else if ( arg instanceof AMQPValue ) {
                rc.setData((AMQPValue)arg);
            } else if ( arg instanceof AMQPSequence ) {
                rc.setData((AMQPSequence)arg);
            } else if ( arg instanceof AMQPType ) {
                rc.setData(new AMQPValue((AMQPType) arg));
            } else if ( arg instanceof Buffer ) {
                rc.setData(new Data((Buffer) arg));
            } else if ( arg instanceof String ) {
                rc.setData(new AMQPValue(new AMQPString((String) arg)));
            } else if ( arg instanceof List) {
                rc.setData(new AMQPSequence((List) arg));
            } else {
                throw new RuntimeException("Unknown type for DataMessage");
            }
        }
        return rc;
    }
    public static Envelope envelope(Object... args) {
        Envelope rc = new Envelope();

        for ( Object arg : args ) {
            if ( arg instanceof Header ) {
                rc.setHeader((Header) arg);
            } else if ( arg instanceof DeliveryAnnotations ) {
                rc.setDeliveryAnnotations((DeliveryAnnotations) arg);
            } else if ( arg instanceof MessageAnnotations ) {
                rc.setMessageAnnotations((MessageAnnotations) arg);
            } else if ( arg instanceof Message) {
                rc.setMessage((Message) arg);
            } else if ( arg instanceof Footer ) {
                rc.setFooter((Footer) arg);
            }
        }

        return rc;
    }

    public static Envelope readAnnotatedMessage(DataInput in) throws Exception {
        try {
            Envelope rc = new Envelope();
            rc.setMessage(new Message());
            Message message = rc.getMessage();

            AMQPType type = TypeReader.read(in);
            if ( type == null ) {
                return rc;
            }

            while (type != null) {
                if ( type instanceof Header ) {
                    if ( rc.getHeader() != null ) {
                        throw new RuntimeException("More than one header section present in message");
                    }
                    rc.setHeader((Header) type);
                } else if ( type instanceof DeliveryAnnotations ) {
                    if ( rc.getDeliveryAnnotations() != null ) {
                        throw new RuntimeException("More than one delivery annotations section present in message");
                    }
                    rc.setDeliveryAnnotations((DeliveryAnnotations) type);
                } else if ( type instanceof MessageAnnotations ) {
                    if ( rc.getMessageAnnotations() != null ) {
                        throw new RuntimeException("More than one message annotations section present in message");
                    }
                    rc.setMessageAnnotations((MessageAnnotations) type);
                } else if ( type instanceof Data || type instanceof AMQPSequence || type instanceof AMQPValue ) {
                    if ( message.getData()!=null ) {
                        throw new RuntimeException("More than one type of application data section present in message");
                    }
                    message.setData(type);
                } else if ( type instanceof Properties ) {
                    if ( message.getProperties() != null ) {
                        throw new RuntimeException("More than one properties section present in message");
                    }
                    message.setProperties((Properties) type);
                } else if ( type instanceof ApplicationProperties ) {
                    if ( message.getApplicationProperties() != null ) {
                        throw new RuntimeException("More than one application properties section present in message");
                    }
                    message.setApplicationProperties((ApplicationProperties) type);
                } else if ( type instanceof Footer ) {
                    if ( rc.getFooter() != null ) {
                        throw new RuntimeException("More than one footer section present in message");
                    }
                    rc.setFooter((Footer) type);
                } else {
                    throw new RuntimeException("Unexpected section found in message : " + type);
                }
                type = TypeReader.read(in);
            }

            return rc;
        } catch (Exception e) {
            throw new RuntimeException("Error reading AnnotatedMessage : " + e.getMessage());
        }
    }

    public static Envelope decodeEnvelope(Buffer buffer) throws Exception {
        return readAnnotatedMessage(new DataByteArrayInputStream(buffer));
    }

}
