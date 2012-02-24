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

import org.fusesource.fabric.apollo.amqp.codec.interfaces.Outcome
import org.fusesource.fabric.apollo.amqp.codec.types.ReceiverSettleMode

/**
 *
 */
abstract trait Receiver extends Link {
  def setCreditHandler(handler: CreditHandler): Unit

  def setMessageHandler(handler: MessageHandler[_]): Unit

  def setSettleMode(mode: ReceiverSettleMode): Unit

  def settle(deliveryId: Long, outcome: Outcome): Unit

  def addLinkCredit(credit: Int): Unit

  def drainLinkCredit: Unit
}