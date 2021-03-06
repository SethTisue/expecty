// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

ThisBuild / version := "0.12.1-SNAPSHOT"
val scala211 = "2.11.12"
val scala212 = "2.12.8"
val scala213 = "2.13.0"
val scalaDotty = "0.16.0-RC3"
ThisBuild / scalaVersion := scala212
ThisBuild / crossScalaVersions := Vector(scala212, scala213, scala211, scalaDotty)

lazy val root = (project in file("."))
  .aggregate(expectyJVM, expectyJS)
  .settings(
    name := "Expecty Root",
    Compile / sources := Nil,
    skip in publish := true,
    commands += Command.command("release") { state =>
      "clean" ::
      s"++${scala212}!" ::
      "expectyJVM/publishSigned" ::
      "expectyJS/publishSigned" ::
      s"++${scala211}!" ::
      "expectyJVM/publishSigned" ::
      "expectyJS/publishSigned" ::
      "expectyNative/publishSigned" ::
      s"++${scala212}!" ::
      "expectyJVM/publishSigned" ::
      "expectyJS/publishSigned" ::
      s"++${scalaDotty}!" ::
      "expectyJVM/publishSigned" ::
      s"++${scala212}!" ::
      state
    },
  )

lazy val utestVersion = "0.6.6"
lazy val utestJVMRef = ProjectRef(uri("git://github.com/eed3si9n/utest.git#79950544"), "utestJVM")
lazy val utestJVMLib = "com.lihaoyi" %% "utest" % utestVersion
lazy val utestJSRef = ProjectRef(uri("git://github.com/eed3si9n/utest.git#79950544"), "utestJS")
lazy val utestJSLib = "com.lihaoyi" %% "utest_sjs0.6" % utestVersion

lazy val expecty = crossProject(JVMPlatform, JSPlatform, NativePlatform).in(file("."))
  .settings(
    name := "Expecty",
    scalacOptions ++= {
      if (scalaVersion.value startsWith "2.10") Nil
      else Seq("-Yrangepos", "-feature", "-deprecation")
    },
    Compile / unmanagedSourceDirectories ++= {
      (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) =>
          Seq((baseDirectory in LocalRootProject).value / "shared" / "src" / "main" / "scala-2.13-beta")
        case _ => Nil
      }) ++
      (CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _))          => Seq((baseDirectory in LocalRootProject).value / "shared" / "src" / "main" / "scala-2")
        case Some((0, _) | (3, _)) => Seq((baseDirectory in LocalRootProject).value / "shared" / "src" / "main" / "scala-3")
        case _ => Nil
      })
    },
    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) => Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value)
      case _ => Nil
    }),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
  .jvmSettings(
    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test,
  )
  .jsSettings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
  )
  .nativeSettings(
    nativeLinkStubs := true
  )

lazy val expectyJVM    = expecty.jvm
  //  .sourceDependency(utestJVMRef % Test, utestJVMLib % Test)
lazy val expectyJS     = expecty.js
  //  .sourceDependency(utestJSRef % Test, utestJSLib % Test)
lazy val expectyNative = expecty.native
  // .settings(
  //   libraryDependencies += "com.lihaoyi" %%% "utest" % utestVersion
  // )
