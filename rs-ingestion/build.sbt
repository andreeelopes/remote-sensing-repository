name := "rs-ingestion"
organization in ThisBuild := "pt.unl.fct"
scalaVersion in ThisBuild := "2.12.7"

// PROJECTS

lazy val global = project
  .in(file("."))
  .settings(
    name := "ingestion",
    settings,
    libraryDependencies ++= commonDependencies
  )

// SETTINGS

lazy val settings = Seq(scalacOptions ++= compilerOptions)

lazy val compilerOptions = Seq(
  "-unchecked",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-deprecation",
  "-encoding",
  "utf8"
)

// DEPENDENCIES

lazy val dependencies =
  new {

    val akkaVersion = "2.5.21"
    val cassandraPluginVersion = "0.93"
    val akkaHttpVersion = "10.1.7"

    val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
    val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
    val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % akkaVersion
    val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % cassandraPluginVersion
    // this allows us to start cassandra from the sample
    val akkaPersistenceCassandraLauncher = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % cassandraPluginVersion
    val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
    val akkaHttpSpray = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
    val lockbackClassid = "ch.qos.logback" % "logback-classic" % "1.2.3"

    // test dependencies
    val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    val commonsIO = "commons-io" % "commons-io" % "2.4" % "test"

    val jodaTime = "joda-time" % "joda-time" % "2.10.1"

    val kryo = "com.github.romix.akka" %% "akka-kryo-serialization" % "0.5.1"

    val playJson = "com.typesafe.play" %% "play-json" % "2.7.2"
    val json = "org.json" % "json" % "20180813"
    val jsonPath = "com.jayway.jsonpath" % "json-path" % "2.4.0"

    val jts = "org.locationtech.jts.io" % "jts-io-common" % "1.16.1"

    val mongo = "org.mongodb.scala" %% "mongo-scala-driver" % "2.6.0"

    val playWS = "com.typesafe.play" %% "play-ahc-ws-standalone" % "2.0.4"
  }



lazy val commonDependencies = Seq(
  dependencies.akkaCluster,
  dependencies.akkaClusterTools,
  dependencies.akkaPersistence,
  dependencies.akkaPersistenceCassandra,
  dependencies.akkaPersistenceCassandraLauncher,
  dependencies.akkaHttp,
  dependencies.akkaHttpSpray,
  dependencies.akkaHttpTestKit,
  dependencies.akkaSlf4j,
  dependencies.lockbackClassid,
  dependencies.akkaTestkit,
  dependencies.scalaTest,
  dependencies.commonsIO,
  dependencies.jodaTime,
  dependencies.kryo,
  dependencies.jsonPath,
  dependencies.playJson,
  dependencies.json,
  dependencies.jts,
  dependencies.mongo,
  dependencies.playWS
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}



//  #### DOCKER #####

enablePlugins(JavaAppPackaging)

// Remove all jar mappings in universal and append the fat jar
mappings in Universal := {
  val universalMappings = (mappings in Universal).value

  val fatJar = (assembly in Compile).value
  val shWait = new File("./scripts/wait-for-it.sh")

  val filtered = universalMappings.filter {
    case (file, name) => !name.endsWith(".jar")
  }

  filtered :+ (fatJar -> ("lib/" + fatJar.getName)) :+ (shWait -> ("lib/" + shWait.getName))
}


dockerUsername := Some("andrelopes")

import com.typesafe.sbt.packager.docker._

dockerCommands := Seq(
  Cmd("FROM", "openjdk:8"),
  Cmd("WORKDIR", "/"),
  Cmd("COPY", "opt/docker/lib/wait-for-it.sh", "/wait-for-it.sh"),
  Cmd("RUN", "chmod", "+x", "/wait-for-it.sh"),
  Cmd("COPY", "opt/docker/lib/*.jar", "/app.jar")
  //  ExecCmd("ENTRYPOINT", "java", "-jar", "/app.jar")
)


