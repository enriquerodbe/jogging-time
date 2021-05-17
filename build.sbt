name := """jogging-time"""
organization := "com.enrique"
version := "1.0-SNAPSHOT"

scalaVersion := "2.13.5"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers ++= Seq(
  Resolver.jcenterRepo,
  "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
)

val silhouetteVersion = "7.0.0"

libraryDependencies ++= Seq(
  guice,
  ws,
  "net.codingwell" %% "scala-guice" % "5.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "com.h2database" % "h2" % "1.4.200",
  "com.mohiva" %% "play-silhouette" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-password-bcrypt" % silhouetteVersion,
  "com.mohiva" %% "play-silhouette-persistence" % silhouetteVersion,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
  "com.mohiva" %% "play-silhouette-testkit" % silhouetteVersion % Test,
)

// Disable warnings for auto-generated files
play.sbt.routes.RoutesKeys.routesImport := Seq.empty

// Scoverage
coverageMinimumStmtTotal := 90
coverageMinimumBranchTotal := 90
coverageFailOnMinimum := true

coverageExcludedPackages := Seq(
  "<empty>",
  "Reverse.*",
  "router",
).mkString(";")
