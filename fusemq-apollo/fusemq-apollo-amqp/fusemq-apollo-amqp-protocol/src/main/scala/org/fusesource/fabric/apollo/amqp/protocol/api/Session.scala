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

/**
 * A representation of an AMQP Session
 *
 * @author Stan Lewis
 */
abstract trait Session {
  /**
   * Sets the outgoing window size for this session
   *
   * @param window
   */
  def setOutgoingWindow(window: Long): Unit

  /**
   * Sets the incoming window size for this session
   *
   * @param window
   */
  def setIncomingWindow(window: Long): Unit

  /**
   * Gets the outgoing window size for this session
   *
   * @return
   */
  def getOutgoingWindow: Long

  /**
   * Gets the incoming window size for this session
   *
   * @return
   */
  def getIncomingWindow: Long

  def attach(link: Link): Unit

  def detach(link: Link): Unit

  def detach(link: Link, reason: String): Unit

  def detach(link: Link, t: Throwable): Unit

  /**
   * Returns whether or not there is sufficient session credit to send based on the remote peer's incoming window
   *
   * @return
   */
  def sufficientSessionCredit: Boolean

  /**
   * Starts this session
   *
   * @param onBegin task to be performed when the peer ackowledges the session has been started
   */
  def begin(onBegin: Runnable): Unit

  /**
   * Returns the connection that created this session
   *
   * @return
   */
  def getConnection: Connection

  /**
   * Returns whether or not this session is associated with a connection and attached to a peer session
   *
   * @return
   */
  def established: Boolean

  /**
   * Sets a LinkListener on this session that will be notified when links are attached or detached
   *
   * @param handler the listener to be called
   */
  def setLinkHandler(handler: LinkHandler): Unit

  def setOnEnd(task: Runnable): Unit

  /**
   * Ends the session gracefully
   */
  def end: Unit

  /**
   * Ends the session ungracefully
   *
   * @param t exception indicating the reason for session termination
   */
  def end(t: Throwable): Unit

  /**
   * Ends the session ungracefully
   *
   * @param reason the reason for session termination
   */
  def end(reason: String): Unit
}