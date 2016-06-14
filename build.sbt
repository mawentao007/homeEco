name := """homeEco"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.xerial" % "sqlite-jdbc" % "3.8.6",
  "com.typesafe.play" %% "play-slick" % "1.1.0"

)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
