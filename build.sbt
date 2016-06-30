name := """homeEco"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  cache,
  ws,
  "com.typesafe.play"   %%   "play-slick"              %   "2.0.0",
  "com.typesafe.play"   %%   "play-slick-evolutions"   %   "2.0.0",
  "org.xerial" % "sqlite-jdbc" % "3.8.6",
  "com.h2database"    %            "h2"                    %   "1.4.187" ,
  "org.postgresql"   %      "postgresql" % "9.4-1206-jdbc4",

specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


routesGenerator := InjectedRoutesGenerator
