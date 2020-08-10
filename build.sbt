name := """jogging-time"""
organization := "com.enrique"
version := "1.0-SNAPSHOT"

scalaVersion := "2.13.3"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers ++= Seq(
  Resolver.jcenterRepo,
  "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
)

val silhouetteVersion = "7.0.0"

libraryDependencies ++= Seq(
  guice,
  ws,
  "net.codingwell" %% "scala-guice" % "4.2.11",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "com.h2database" % "h2" % "1.4.200",
  "com.mohiva" %% "play-silhouette" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2",
  "com.enragedginger" %% "akka-quartz-scheduler" % "1.8.4-akka-2.6.x",
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
)

play.sbt.routes.RoutesKeys.routesImport := Seq.empty
