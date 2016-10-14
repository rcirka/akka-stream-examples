name := "akka-streams"

version := "1.0"

scalaVersion := "2.11.8"


libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.10",
  "com.typesafe.akka" %% "akka-stream" % "2.4.10",
  "com.typesafe.play" %% "play-json" % "2.4.8",
  "com.typesafe.play" %% "play-ws" % "2.4.8",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.reactivemongo" %% "reactivemongo" % "0.11.14",
  "org.typelevel" %% "cats" % "0.7.2"
)

