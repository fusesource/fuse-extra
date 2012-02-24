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

import org.fusesource.fabric.apollo.amqp.codec.api.AnnotatedMessage
import org.fusesource.fabric.apollo.amqp.codec.api.BareMessage
import org.fusesource.fabric.apollo.amqp.codec.types.SenderSettleMode
import org.fusesource.hawtbuf.Buffer

/**
 *
 */
abstract trait Sender extends Link {
  /**
   * Returns whether or not link credit is available to send messages
   * @return
   */
  def full: Boolean

  /**
   * Send a message over this link, returns false if the message cannot
   * be sent at this time due to lack of link credit
   * @param message
   * @return
   */
  def offer(message: Buffer): Boolean

  /**
   * Send a message over this link, returns false if the message cannot
   * be sent at this time due to lack of link credit
   * @param message
   * @return
   */
  def offer(message: AnnotatedMessage): Boolean

  /**
   * Send a message over this link, returns false if the message cannot
   * be sent at this time due to lack of link credit
   * @param message
   * @return
   */
  def offer(message: BareMessage[_]): Boolean

  /**
   * Sets the tagger used to set delivery tags for outgoing messages
   * @param tagger
   */
  def setTagger(tagger: DeliveryTagger): Unit

  /**
   * Sets the AvailableHandler that will be called when there is a need
   * to update the peer with this sender's flow state
   * @param handler
   */
  def setAvailableHandler(handler: AvailableHandler): Unit

  /**
   * Sets the acknowledgement handler for this sender
   * @param handler
   */
  def setAckHandler(handler: AckHandler): Unit

  /**
   * Sets the settle mode for this sender
   * @param mode
   */
  def setSettleMode(mode: SenderSettleMode): Unit

  /**
   * Sets the task that the sender will call when there is sufficient
   * link credit to send messages
   * @param refiller
   */
  def refiller(refiller: Runnable): Unit

  /**
   * Gets the current link credit available to this sender
   * @return
   */
  def getLinkCredit: Int
}