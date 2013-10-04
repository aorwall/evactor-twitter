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

import akka.actor._
import akka.kernel.Bootable
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import java.nio.channels.ClosedChannelException
import org.evactor.model.events.Event
import org.evactor.subscribe.Subscriptions
import unfiltered.netty.websockets._
import unfiltered.request.{Path, Seg}
import unfiltered.response.{BadRequest, ResponseString}
import scala.collection.mutable.HashMap

object ExampleKernel {
  //test
  def main(args: Array[String]){
    val foo = new ExampleKernel
    foo.startup()
  }
}

class ExampleKernel extends Bootable {

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  val activeWebsockets = HashMap[String, ActorRef]()
  
  lazy val system = ActorSystem("twitterExample")
  
  // context
  lazy val context = system.actorOf(Props[EvactorContext], name = "evactor")
  
  // api server port
  val port = if(system.settings.config.hasPath("evactor.api.port")){
    system.settings.config.getInt("evactor.api.port")
  } else {
    8080
  }
  
  lazy val indexFile = io.Source.fromInputStream(getClass.getResourceAsStream("/index.html")).mkString
  
  def sendEvent(s: WebSocket)(e: Event): Unit = {
    println(mapper.writeValueAsString(e))
    s.send(mapper.writeValueAsString(e))
  }
  
  lazy val nettyServer = unfiltered.netty.Http(port)
    .handler(unfiltered.netty.websockets.Planify({
      case Path(Seg("socket" :: channel :: Nil))  => {
        case Open(s) => {
          val actor = system.actorOf(Props(new WebsocketProducer(Subscriptions(channel), sendEvent(s) _)), name = s.channel.getId.toString)
          activeWebsockets += s.channel.getId.toString->actor
        }
        case Message(s, Text(msg)) => 
        case Close(s) => killActor(s.channel.getId.toString); 
        case Error(s, e) => e match {
          case e: ClosedChannelException => killActor(s.channel.getId.toString) 
          case _ => e.printStackTrace
        }
      }}).onPass(_.sendUpstream(_)))
    .handler(unfiltered.netty.cycle.Planify({
      case Path(Seg(Nil)) => try {
        ResponseString(indexFile)
      } catch { case e => BadRequest }
      case _ => ResponseString("Couldn't handle request")
    }))
  
  def killActor(id: String){
     activeWebsockets.remove(id) match {
       case Some(actor) => system.stop(actor)
       case None => 
     }
  }  
    
  def startup = {
    if(!system.settings.config.hasPath("evactor")) throw new RuntimeException("No configuration found!")
    context  // Start evactor context
    nettyServer.run()
  }

  def shutdown = {
    system.shutdown()
    nettyServer.stop()
  }
}

