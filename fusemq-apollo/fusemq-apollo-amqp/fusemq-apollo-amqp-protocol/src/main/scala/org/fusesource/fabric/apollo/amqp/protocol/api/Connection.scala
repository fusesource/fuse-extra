/**
 * Copyright (C) FuseSource, Inc.
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
package org.fusesource.fabric.apollo.amqp.protocol.api

import org.fusesource.hawtdispatch.DispatchQueue

/**
 * Represents an AMQP Connection.
 *
 * TODO - Add more getters/setters for fields in Open frame
 *
 * @author Stan Lewis
 */
abstract trait Connection {
  def setIdleTimeout(timeout: Long): Unit

  def getIdleTimeout: Long

  /**
   * Connects this connection to a peer
   *
   * @param uri the URI of peer
   */
  def connect(uri: String): Unit

  def onConnected(task: Runnable): Unit

  def onDisconnected(task: Runnable): Unit

  /**
   * Creates a new session on this connection.
   *
   * @return a new un-established instance of a {@link Session}
   */
  def createSession: Session

  /**
   * Returns whether or not this Connection is connected
   *
   * @return boolean
   */
  def isConnected: Boolean

  /**
   * Gets the last error received on this Connection
   *
   * @return {@link Throwable}
   */
  def error: Throwable

  /**
   * Gets the dispatch queue used by this Connection
   *
   * @return {@link org.fusesource.hawtdispatch.DispatchQueue}
   */
  def getDispatchQueue: DispatchQueue

  /**
   * Sets the container ID to be used by this Connection
   *
   * @param id
   */
  def setContainerID(id: String): Unit

  def getContainerID: String

  /**
   * Closes this connection gracefully.
   */
  def close: Unit

  /**
   * Closes this connection ungracefully
   *
   * @param t the exception causing the connection to be closed
   */
  def close(t: Throwable): Unit

  /**
   * Closes this connection ungracefully
   *
   * @param reason the reason the connection is being closed
   */
  def close(reason: String): Unit

  def setSessionHandler(handler: SessionHandler): Unit

  /**
   * Gets the container ID of this Connection's peer
   *
   * @return {@link String}}
   */
  def getPeerContainerID: String
}