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
package org.evactor.twitter.transformer

import org.evactor.transform.Transformer
import org.evactor.twitter.StatusEvent
import akka.actor.ActorLogging
import akka.actor.ActorRef
import org.evactor.monitor.Monitored
import twitter4j.Status

class TwitterJsonToStatusEvent(collector: ActorRef)  extends Transformer with Monitored with ActorLogging {
  import context.dispatcher
    
  def receive = {
    case status: Status => transform(status)
    case msg => log.info("can't handle {}", msg)
  }
  
  def transform(status: Status) {
    log.info(status.getText)

    val text = status.getText
    val id = status.getId.toString
    val timestamp = status.getCreatedAt.getTime
    val entities = status.getHashtagEntities
    val urlEntities = status.getURLEntities
    val screenName = status.getUser.getScreenName

    val hashtags: List[String] = for(entity <- entities.toList) yield (entity.getText)
    val urls: List[String] = for(e <- urlEntities.toList) yield (e.getExpandedURL)

    incr("twittertransformer:success")
    collector ! new StatusEvent(id, timestamp, screenName, text, urls, hashtags)

  }
  
  
}