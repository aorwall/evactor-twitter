/*
 * Copyright 2012 Albert Örwall
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.evactor

import akka.actor.ActorLogging
import org.evactor.subscribe.Subscription
import org.evactor.process.Processor
import org.evactor.model.events.Event

class WebsocketProducer (
    override val subscriptions: List[Subscription],
    val send: (Event) => Unit)
  extends Processor (subscriptions) 
  with ActorLogging {
  
  protected def process(event: Event) {
    send(event)
  }

  override def preStart = {
    log.info("Starting...")
    super.preStart()
  }
  
  
  override def postStop = {
    log.info("Stopping...")
    super.postStop();
  }
  
}