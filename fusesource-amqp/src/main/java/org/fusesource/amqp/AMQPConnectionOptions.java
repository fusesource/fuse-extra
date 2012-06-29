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

import org.fusesource.amqp.callback.AMQPConnection;
import org.fusesource.amqp.types.AMQPSymbol;
import org.fusesource.hawtdispatch.transport.Transport;

import java.util.Map;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class AMQPConnectionOptions {

    private Logger logger = new Logger();

    private String containerId;
    private String hostName;
    private long idleTimeout = 90000;
    private int maxFrameSize = 1024*1024*1024*2 - 1;
    private AMQPSymbol[] desiredCapabilities;
    private AMQPSymbol[] offeredCapabilities;

//    = new AMQPSymbol[]{
//        new AMQPSymbol(ascii("APACHE.ORG:NO_LOCAL")),
//        new AMQPSymbol(ascii("APACHE.ORG:SELECTOR"))
//    };
    private AMQPSymbol[] incomingLocales;
    private AMQPSymbol[] outgoingLocales;
    private Map properties;
    private long sessionHandleMax = 1024*1024*1024*2 - 1;
    private String userName;
    private String password;

    public AMQPConnectionOptions() {
    }

    public AMQPConnectionOptions(AMQPConnectionOptions other) {
        this.setLogger(other.getLogger());
        this.setContainerId(other.getContainerId());
        this.setHostName(other.getHostName());
        this.setIdleTimeout(other.getIdleTimeout());
        this.setMaxFrameSize(other.getMaxFrameSize());
        this.setDesiredCapabilities(other.getDesiredCapabilities());
        this.setOfferedCapabilities(other.getOfferedCapabilities());
        this.setIncomingLocales(other.getIncomingLocales());
        this.setProperties(other.getProperties());
        this.setSessionHandleMax(other.getSessionHandleMax());
        this.setUserName(other.getUserName());
        this.setPassword(other.getPassword());
    }

    public AMQPConnectionOptions copy() {
        return new AMQPConnectionOptions(this);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public long getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public int getMaxFrameSize() {
        return maxFrameSize;
    }

    public void setMaxFrameSize(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
    }

    public AMQPSymbol[] getDesiredCapabilities() {
        return desiredCapabilities;
    }

    public void setDesiredCapabilities(AMQPSymbol[] desiredCapabilities) {
        this.desiredCapabilities = desiredCapabilities;
    }

    public AMQPSymbol[] getOfferedCapabilities() {
        return offeredCapabilities;
    }

    public void setOfferedCapabilities(AMQPSymbol[] offeredCapabilities) {
        this.offeredCapabilities = offeredCapabilities;
    }

    public AMQPSymbol[] getIncomingLocales() {
        return incomingLocales;
    }

    public void setIncomingLocales(AMQPSymbol[] incomingLocales) {
        this.incomingLocales = incomingLocales;
    }

    public AMQPSymbol[] getOutgoingLocales() {
        return outgoingLocales;
    }

    public void setOutgoingLocales(AMQPSymbol[] outgoingLocales) {
        this.outgoingLocales = outgoingLocales;
    }

    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public long getSessionHandleMax() {
        return sessionHandleMax;
    }

    public void setSessionHandleMax(long sessionHandleMax) {
        this.sessionHandleMax = sessionHandleMax;
    }

    static public class Logger {
        public void debug(String format, Object... args) {
            _debug(format, args);
        }
        public void trace(String format, Object... args) {
            _trace(format, args);
        }
        protected void _debug(String format, Object[] args) {
        }
        protected void _trace(String format, Object[] args) {
        }
    }

    static public class ConsoleLogger extends Logger {
        public void debug(String format, Object... args) {
            System.out.println("[debug] "+String.format(format, args));
        }
        public void trace(String format, Object... args) {
            System.out.println("[trace] "+String.format(format, args));
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
