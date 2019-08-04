import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

version in ThisBuild := "0.2.1"
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
    publish / skip := true
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
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.7"
    )
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
      "org.akka-js" %%% "akkajsactortyped" % "1.2.5.21"
    )
  )
  .dependsOn(ui)

lazy val akkaJVM = akka.jvm
lazy val akkaJS = akka.js


lazy val zio = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "yachiyo-zio",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % "0.6.7" % "test",
      "dev.zio" %% "zio" % "1.0.0-RC10-1",
      "dev.zio" %% "zio-streams" % "1.0.0-RC10-1"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.6.7" % "test",
      "dev.zio" %%% "zio" % "1.0.0-RC10-1",
      "dev.zio" %%% "zio-streams" % "1.0.0-RC10-1"
    )
  )
  .dependsOn(ui)

lazy val zioJVM = zio.jvm
lazy val zioJS = zio.js

lazy val demo = project
  .in(file("demo"))
  .dependsOn(akkaJS)
  .enablePlugins(ScalaJSPlugin)
  .settings(
    name := "yachiyo-demo",
    resolvers += "yuiwai repo" at "https://s3-us-west-2.amazonaws.com/repo.yuiwai.com",
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.7",
      "com.yuiwai" %%% "kasumi-core" % "0.2.0-SNAPSHOT"
    ),
    scalaJSUseMainModuleInitializer := true
  )

lazy val demoZio = project
  .in(file("demo-zio"))
  .dependsOn(zioJVM)
  .settings(
    name := "yachiyo-demo-zio"
  )

lazy val fx = project
  .in(file("fx"))
  .settings(
    name := "yachiyo-fx",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "8.0.192-R14"
    )
  )
  .dependsOn(uiJVM)
