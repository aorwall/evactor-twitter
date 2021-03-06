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
import sbt._
import Keys._
import com.typesafe.sbt.SbtStartScript

object EvactorBuild extends Build {
  
  import Dependency._
  
  val Organization = "org.evactor"
  val ScalaVersion = "2.10.2"
  val Version      = "0.4-SNAPSHOT"

  lazy val evactorTwitter = Project(
    id = "evactor-twitter",
    base = file("."),
    settings = defaultSettings ++ SbtStartScript.startScriptForClassesSettings ++ Seq(
      libraryDependencies ++= Seq (evactorCore, akkaKernel, akkaSlf4j, logback, jacksonCore, jacksonMapper, jacksonScala, unfilteredFilter, unfilteredNetty, unfilteredNettyServer, unfilteredWebsockets, twitter4j, twitter4jStream)
    )
  )

  override lazy val settings = super.settings ++ Seq(
        resolvers += "Sonatype Snapshot Repository" at "https://oss.sonatype.org/content/repositories/snapshots/"
  )
  
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
    organization := Organization,
    version      := Version,
    scalaVersion := ScalaVersion,
    crossPaths   := false
  ) 

  lazy val defaultSettings = buildSettings ++ Seq(
	  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
	  javacOptions  ++= Seq("-Xlint:unchecked", "-Xlint:deprecation")
  )  

}

object Dependency {

  // Versions
  object V {
    val Evactor = "0.5-SNAPSHOT"
    val Scalatest = "1.9.1"
    val Akka = "2.1.4"
    val Jackson = "2.1.3"
    val Slf4j = "1.6.4"
    val Unfiltered = "0.6.8"
  }

  val akkaKernel = "com.typesafe.akka" % "akka-kernel_2.10" % V.Akka
  val akkaSlf4j = "com.typesafe.akka" % "akka-slf4j_2.10" % V.Akka
  val evactorCore = "org.evactor" % "core" % V.Evactor
  val jacksonMapper = "com.fasterxml.jackson.core" % "jackson-databind" % V.Jackson
  val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % V.Jackson
  val jacksonScala = "com.fasterxml.jackson.module" % "jackson-module-scala_2.10" % V.Jackson
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime"
  val slf4jApi = "org.slf4j" % "slf4j-api" % V.Slf4j
  val twitter4j = "org.twitter4j" % "twitter4j-core" % "3.0.3"
  val twitter4jStream = "org.twitter4j" % "twitter4j-stream" % "3.0.3"
  val unfilteredFilter = "net.databinder" % "unfiltered-filter_2.10" % V.Unfiltered
  val unfilteredNetty = "net.databinder" % "unfiltered-netty_2.10" % V.Unfiltered
  val unfilteredNettyServer = "net.databinder" % "unfiltered-netty-server_2.10" % V.Unfiltered
  val unfilteredWebsockets = "net.databinder" % "unfiltered-netty-websockets_2.10" % V.Unfiltered
  
}

  
