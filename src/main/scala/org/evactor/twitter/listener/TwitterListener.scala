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
package org.evactor.twitter.listener

import org.evactor.listen.Listener
import akka.actor.ActorLogging
import akka.actor.ActorRef
import org.evactor.Start
import org.evactor.monitor.Monitored
import twitter4j._

class TwitterListener(sendTo: ActorRef) extends Listener with Monitored with ActorLogging {

  class TweetListener extends StatusListener {
    def onStatus(status: Status) {
      sendTo ! status
    }
    def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice) {}
    def onTrackLimitationNotice(numberOfLimitedStatuses: Int) {}
    def onException(ex: Exception) {
      log.error("failure", ex)
    }
    def onStallWarning(warning: StallWarning) {}
    def onScrubGeo(userId: Long, upToStatusId: Long) {}
  }
  
  def receive = {
    case Start => read()
    case msg => log.debug("can't handle {}", msg)
  }

  private[this] def read(){
    // reads oauth credentials from twitter4j.properties
    val twitterStream = new TwitterStreamFactory().getInstance()

    twitterStream.addListener(new TweetListener)

    // sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
    twitterStream.sample();

  }
  
  override def preStart = {
    super.preStart
    self ! Start
  }
  
}