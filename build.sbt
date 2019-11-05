import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val scalaVersion_2_12 = "2.12.8"
val scalaVersion_2_13 = "2.13.0"
val scalaVersion_2_11 = "2.11.11"
val utestVersion = "0.6.9"

version in ThisBuild := "0.3.0-SNAPSHOT"
scalaVersion in ThisBuild := scalaVersion_2_12
organization in ThisBuild := "com.yuiwai"
scalacOptions in ThisBuild ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
)

lazy val root = project
  .in(file("."))
  .aggregate(
    coreJVM, coreJS,
    uiJVM, uiJS, uiNative,
    plainJVM, plainJS, plainNative,
    akkaJVM, akkaJS,
    zioJVM, zioJS)
  .settings(
    name := "yachiyo",
    publish / skip := true
  )

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "yachiyo-core",
    crossScalaVersions := Seq(scalaVersion_2_11, scalaVersion_2_12, scalaVersion_2_13),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    publishTo := Some(Resolver.file("file", file("release")))
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % utestVersion % "test"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % utestVersion % "test",
    )
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val ui = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "yachiyo-ui",
    crossScalaVersions := Seq(scalaVersion_2_13, scalaVersion_2_12, scalaVersion_2_11),
    publishTo := Some(Resolver.file("file", file("release")))
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.9.7"
    )
  )
  .nativeSettings(
    crossScalaVersions := Nil,
    scalaVersion := scalaVersion_2_11
  )

lazy val uiJS = ui.js
lazy val uiJVM = ui.jvm
lazy val uiNative = ui.native

lazy val plain = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "yachiyo-plain",
    crossScalaVersions := Seq(scalaVersion_2_12, scalaVersion_2_11),
    publishTo := Some(Resolver.file("file", file("release")))
  )
  .nativeSettings(
    crossScalaVersions := Nil,
    scalaVersion := scalaVersion_2_11
  )
  .dependsOn(ui)

lazy val plainJVM = plain.jvm
lazy val plainJS = plain.js
lazy val plainNative = plain.native

lazy val akka = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .settings(
    name := "yachiyo-akka",
    crossScalaVersions := Seq(scalaVersion_2_12, scalaVersion_2_11),
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
    publishTo := Some(Resolver.file("file", file("release"))),
    crossScalaVersions := Seq(scalaVersion_2_12, scalaVersion_2_13),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "utest" % utestVersion % "test",
      "dev.zio" %% "zio" % "1.0.0-RC10-1",
      "dev.zio" %% "zio-streams" % "1.0.0-RC10-1"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % utestVersion % "test",
      "dev.zio" %%% "zio" % "1.0.0-RC10-1",
      "dev.zio" %%% "zio-streams" % "1.0.0-RC10-1"
    )
  )
  .dependsOn(ui)

lazy val zioJVM = zio.jvm
lazy val zioJS = zio.js

lazy val demo = project
  .in(file("demo"))
  .dependsOn(akkaJS, coreJS)
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

lazy val drawing = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("drawing"))
  .settings(
    name := "yachiyo-drawing",
    crossScalaVersions := Seq(scalaVersion_2_11, scalaVersion_2_12, scalaVersion_2_13),
  )
  .dependsOn(core)

lazy val fx = project
  .in(file("fx"))
  .settings(
    name := "yachiyo-fx",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "8.0.192-R14"
    )
  )
  .dependsOn(uiJVM, coreJVM)
