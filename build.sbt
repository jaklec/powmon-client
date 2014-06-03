name := "powmon-client"

organization := "se.jaklec"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies ++= Seq(
  "se.jaklec" %% "gpio-scala" % "0.1-SNAPSHOT",
  "org.scalatest" %% "scalatest" % "2.1.2" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.1.0" % "test",
  "com.typesafe.akka" %% "akka-actor" % "2.1.0",
  "org.mockito" % "mockito-all" % "1.9.5" % "test"
)

