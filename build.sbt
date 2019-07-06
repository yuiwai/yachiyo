import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

version in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.12.8"

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "yachiyo-core",
    testFrameworks += new TestFramework("utest.runner.Framework")
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
    name := "yachiyo-ui"
  )
  .dependsOn(core)

lazy val uiJS = ui.js
lazy val uiJVM = ui.jvm

lazy val akka = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "yachiyo-akka"
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

lazy val akkJVM = akka.jvm
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
