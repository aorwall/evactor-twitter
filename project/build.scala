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
import sbtassembly.Plugin._
import AssemblyKeys._

object BamBuild extends Build {
  
  import Dependency._
  
  val Organization = "org.evactor"
  val ScalaVersion = "2.10.2"
  val Version      = "0.3-SNAPSHOT"

  lazy val evactorTwitter = Project(
    id = "evactor-twitter",
    base = file("."),
    settings = defaultSettings ++ exampleAssemblySettings ++ Seq(
      libraryDependencies ++= Seq (evactorCore, akkaKernel, akkaSlf4j, httpClient, logback, Test.scalatest, Test.junit, Test.akkaTestkit, jacksonCore, jacksonMapper, jacksonScala, unfilteredFilter, unfilteredNetty, unfilteredNettyServer, unfilteredWebsockets)
    )
  )

  override lazy val settings = super.settings ++ Seq(
        resolvers += "Typesafe Snapshot Repository" at "http://repo.typesafe.com/typesafe/snapshots/"
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

  lazy val exampleAssemblySettings = assemblySettings ++ Seq(
    test in assembly := {},
	excludedJars in assembly <<= (fullClasspath in assembly) map { cp => 
	  cp filter { x => x.data.getName == "uuid-3.2.0.jar" ||  x.data.getName == "slf4j-log4j12-1.6.1.jar" || x.data.getName == "log4j-1.2.16.jar" || x.data.getName == "commons-logging-1.1.1.jar" }},
	excludedFiles in assembly := { (bases: Seq[File]) =>
	  bases flatMap { base =>
        (base / "META-INF" * "*").get collect { 
          case f if f.getName.toLowerCase == "license" => f
          case f if f.getName.toLowerCase == "license.txt" => f
          case f if f.getName.toLowerCase == "manifest.mf" => f
          case f if f.getName.toLowerCase == "notice.txt" => f
          case f if f.getName.toLowerCase == "notice" => f
          case f if f.getName.toLowerCase == "dependencies" => f
          case f if f.getName.toLowerCase == "spring.tooling" => f
          case f if f.getName.toLowerCase == "index.list" => f
	    }
	  }
	},
	mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
	  {
	    case "application.conf" => MergeStrategy.concat
            case "overview.html" => MergeStrategy.concat
	    case x => old(x)
	  }
	},
	mainClass in assembly := Some("org.evactor.ExampleKernel"),
	jarName in assembly := "evactorTwitter.jar"
  )
}

object Dependency {

  // Versions
  object V {
    val Akka = "2.1.2"
    val Jackson = "2.0.2"
    val Scalatest = "1.9.1"
    val Slf4j = "1.6.4"
    val Unfiltered = "0.6.8"
  }

  val evactorCore = "org.evactor" % "core" % "0.3-SNAPSHOT"
  val akkaKernel = "com.typesafe.akka" % "akka-kernel_2.10" % V.Akka
  val akkaSlf4j = "com.typesafe.akka" % "akka-slf4j_2.10" % V.Akka
  val httpClient = "org.apache.httpcomponents" % "httpclient" % "4.1"
  val jacksonMapper = "com.fasterxml.jackson.core" % "jackson-databind" % V.Jackson
  val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % V.Jackson
  val jacksonScala = "com.fasterxml.jackson.module" % "jackson-module-scala" % V.Jackson
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.0" % "runtime"
  val slf4jApi = "org.slf4j" % "slf4j-api" % V.Slf4j
  val unfilteredFilter = "net.databinder" % "unfiltered-filter_2.10" % V.Unfiltered
  val unfilteredNetty = "net.databinder" % "unfiltered-netty_2.10" % V.Unfiltered
  val unfilteredNettyServer = "net.databinder" % "unfiltered-netty-server_2.10" % V.Unfiltered
  val unfilteredWebsockets = "net.databinder" % "unfiltered-netty-websockets_2.10" % V.Unfiltered

  object Test {
    val junit = "junit" % "junit" % "4.4" % "test"
    val scalatest = "org.scalatest" % "scalatest_2.10" % V.Scalatest % "test"
    val mockito = "org.mockito" % "mockito-core" % "1.8.1" % "test"
    val akkaTestkit = "com.typesafe.akka" % "akka-testkit_2.10" % V.Akka % "test"
  }
  
}

  
