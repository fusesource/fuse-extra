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

import org.fusesource.fabric.apollo.amqp.codec.interfaces.Source
import org.fusesource.fabric.apollo.amqp.codec.interfaces.Target
import org.fusesource.fabric.apollo.amqp.codec.types.Role

/**
 *
 */
abstract trait Link {
  def getName: String

  def setName(name: String): Unit

  def getRole: Role

  def setMaxMessageSize(size: Long): Unit

  def getMaxMessageSize: Long

  def setSource(source: Source): Unit

  def setTarget(target: Target): Unit

  def getSource: Source

  def getTarget: Target

  def onAttach(task: Runnable): Unit

  def onDetach(task: Runnable): Unit

  def established: Boolean

  def getSession: Session
}