


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


// Compiler options from: https://alexn.org/blog/2020/05/26/scala-fatal-warnings/
lazy val compilerOptions = Seq(
  // Feature options
  "-encoding", "utf-8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ymacro-annotations",

  // Warnings as errors!
  "-Xfatal-warnings",

  // Linting options
  "-unchecked",
  "-Xcheckinit",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:deprecation",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Wdead-code",
  "-Wextra-implicit",
  "-Wnumeric-widen",
  "-Wunused:imports", 
  "-Wunused:locals",
  "-Wunused:patvars",
  "-Wunused:privates",
  "-Wvalue-discard",

  // Deactivated to avoid many false positives from 'evidence' parameters in context bounds
//  "-Wunused:params",
//  "-Wunused:implicits",
)


lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= 
    Seq("Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository") ++
    Resolver.sonatypeOssRepos("releases") ++
    Resolver.sonatypeOssRepos("snapshots")
)

