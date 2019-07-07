import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

version in ThisBuild := "0.1.0"
scalaVersion in ThisBuild := "2.12.8"
organization in ThisBuild := "com.yuiwai"
scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
)

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS, uiJVM, uiJS, akkaJVM, akkaJS)
  .settings(
    name := "yachiyo",
    publish := {},
    publishLocal := {}
  )

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "yachiyo-core",
    testFrameworks += new TestFramework("utest.runner.Framework"),
    publishTo := Some(Resolver.file("file", file("release")))
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % "0.6.7" % "test"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.6.7" % "test",
      "org.akka-js" %%% "akkajsactortyped" % "1.2.5.21"
    )
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val ui = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "yachiyo-ui",
    publishTo := Some(Resolver.file("file", file("release")))
  )
  .dependsOn(core)

lazy val uiJS = ui.js
lazy val uiJVM = ui.jvm

lazy val akka = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "yachiyo-akka",
    publishTo := Some(Resolver.file("file", file("release")))
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor-typed" % "2.5.21"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.akka-js" %%% "akkajsactortyped" % "1.2.5.21",
      "org.scala-js" %%% "scalajs-dom" % "0.9.7"
    )
  )
  .dependsOn(ui)

lazy val akkaJVM = akka.jvm
lazy val akkaJS = akka.js

lazy val demo = project
  .in(file("demo"))
  .dependsOn(akkaJS)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "yachiyo-demo",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.7"
    ),
    scalaJSUseMainModuleInitializer := true
  )
