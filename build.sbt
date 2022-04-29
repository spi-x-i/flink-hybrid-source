import sbt._
import sbt.Keys._

lazy val `flink-hybrid-source` = (project in file("."))
  .settings(
    name := "flink-hybrid-source",
    scalaVersion := "2.12.11",
    organization := "io.radicalbit"
  )
  .settings(
    resolvers in ThisBuild ++= Seq(
      "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
      "jitpack" at "https://jitpack.io",
      Opts.resolver.sonatypeSnapshots,
      Resolver.mavenLocal
    )
  )
  .settings(
    libraryDependencies ++= Dependencies.flinkDependencies
  )
  .settings(
    test in assembly := {},
    assemblyOption in assembly := (assemblyOption in assembly).value
      .withCacheUnzip(false)
      .withIncludeScala(false)
      .withCacheOutput(false),
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", "io.netty.versions.properties") => MergeStrategy.filterDistinctLines
      case PathList("META-INF", "services", _)                  => MergeStrategy.first
      case "module-info.class"                                  => MergeStrategy.last
      case "log4j.properties"                                   => MergeStrategy.last
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    // mainClass in assembly := Some("io.radicalbit.rtsae.PipelineMaterializer"),
    assemblyJarName in assembly := sysPropOrDefault("fat.jar.name", "flink-hybrid-source.jar")
  )
  .settings(
    credentials ++= Seq(Credentials(Path.userHome / ".artifactory" / ".credentials")),
    parallelExecution in Test := false,
    scalafmtOnCompile in ThisBuild := true
  )

def sysPropOrDefault(propName: String, default: String): String =
  Option(System.getProperty(propName)).getOrElse(default)
