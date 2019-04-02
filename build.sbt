name := "rs-repo"
organization in ThisBuild := "fct.unl"
scalaVersion in ThisBuild := "2.12.7"

// PROJECTS

lazy val global = project
  .in(file("."))
  .settings(settings)
  .aggregate(
    indexer,
    ingestion
  )

lazy val indexer = project
  .settings(
    name := "indexer-app",
    settings,
    libraryDependencies ++= commonDependencies ++ Seq()
  )

lazy val ingestion = project
  .settings(
    name := "ingestion-app",
    settings,
    assemblySettings,
    libraryDependencies ++= commonDependencies ++ Seq()
  )
//  .dependsOn(
//    common
//  )


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
    val akkaHttpXML = "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion

    val akkaSlf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
    val lockbackClassid = "ch.qos.logback" % "logback-classic" % "1.2.3"

    // test dependencies
    val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
    val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
    val commonsIO = "commons-io" % "commons-io" % "2.4" % "test"
  }

lazy val commonDependencies = Seq(
  dependencies.akkaCluster,
  dependencies.akkaClusterTools,
  dependencies.akkaPersistence,
  dependencies.akkaPersistenceCassandra,
  dependencies.akkaPersistenceCassandraLauncher,
  dependencies.akkaHttp,
  dependencies.akkaHttpXML,
  dependencies.akkaSlf4j,
  dependencies.lockbackClassid,
  dependencies.akkaTestkit,
  dependencies.scalaTest,
  dependencies.commonsIO
)

// SETTINGS

lazy val settings = commonSettings


lazy val commonSettings = Seq(
  scalacOptions ++= compilerOptions,
  resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  )
)


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


lazy val assemblySettings = Seq(
  assemblyJarName in assembly := name.value + ".jar",
  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs@_*) => MergeStrategy.discard
    case _ => MergeStrategy.first
  }
)