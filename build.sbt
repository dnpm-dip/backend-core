


name := "core"
ThisBuild / organization := "de.dnpm.dip"
ThisBuild / scalaVersion := "2.13.13"
ThisBuild / version      := "1.0-SNAPSHOT"


//-----------------------------------------------------------------------------
// PROJECT
//-----------------------------------------------------------------------------

lazy val root =
  project.in(file("."))
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scalatest"       %% "scalatest"                  % "3.2.18" % Test,
        "org.slf4j"           %  "slf4j-api"                  % "2.0.9",
        "com.chuusai"         %% "shapeless"                  % "2.3.10",
        "org.typelevel"       %% "cats-core"                  % "2.9.0",
        "com.typesafe.play"   %% "play-json"                  % "2.9.4",
        "com.github.andyglow" %% "scala-jsonschema"           % "0.7.11",
        "com.github.andyglow" %% "scala-jsonschema-cats"      % "0.7.11",
        "com.github.andyglow" %% "scala-jsonschema-play-json" % "0.7.11"
     )
  )


//-----------------------------------------------------------------------------
// SETTINGS
//-----------------------------------------------------------------------------

lazy val settings = commonSettings

lazy val compilerOptions = Seq(
  "-encoding", "utf8",
  "-unchecked",
  "-Xfatal-warnings",
  "-feature",
  "-language:higherKinds",
  "-language:postfixOps",
  "-deprecation"
)

lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= 
    Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository") ++
    Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")
)

