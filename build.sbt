name := "body-measurement-api"

organization := "hpi"

version := "1.0"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    scalaVersion := "2.12.2"
  )

libraryDependencies += guice
libraryDependencies += "net.codingwell" %% "scala-guice" % "4.1.0"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "3.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0"
libraryDependencies += "com.h2database" % "h2" % "1.4.193"

libraryDependencies += "org.scalatestplus.play" % "scalatestplus-play_2.12" % "3.0.0" % "test"
libraryDependencies += specs2 % Test


// documentation

sources in LWM <++= baseDirectory map (d => (d / "README.md").get )
targetDirectory in LWM <<= baseDirectory(_ / "public")
cssFile in LWM <<= baseDirectory( d => Some(d / "public" / "css" / "main.css"))
encoding in LWM := "UTF-8"
