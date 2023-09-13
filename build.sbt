


name := "core"
ThisBuild / organization := "de.dnpm.dip"
ThisBuild / scalaVersion := "2.13.12"
ThisBuild / version      := "1.0-SNAPSHOT"


//-----------------------------------------------------------------------------
// PROJECT
//-----------------------------------------------------------------------------

lazy val root =
  project.in(file("."))
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        "org.scalatest"      %% "scalatest"   % "3.1.1" % Test,
        "org.slf4j"          %  "slf4j-api"   % "1.7.32",
        "com.chuusai"        %% "shapeless"   % "2.3.10",
        "org.typelevel"      %% "cats-core"   % "2.9.0",
        "com.typesafe.play"  %% "play-json"   % "2.9.2",
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
//  "-language:existentials",
  "-language:higherKinds",
//  "-language:implicitConversions",
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

