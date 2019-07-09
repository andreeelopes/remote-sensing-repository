

import java.io.File
import java.util.concurrent.CountDownLatch

import akka.actor.{ActorRef, ActorSystem}
import akka.persistence.cassandra.testkit.CassandraLauncher
import api.{Services, ServerStart}
import com.typesafe.config.{Config, ConfigFactory}
import protocol.master.MasterSingleton
import protocol.scheduler.Orchestrator
import protocol.worker.Worker


object Main {

  // note that 2551 and 2552 are expected to be seed nodes though, even if
  // the master starts at 2000

  val masterPortRange = 2000 to 2999

  val orchestratorPortRange = 3000 to 3999

  val apiActorPortRange = 6000 to 6999

  def main(args: Array[String]): Unit = {
    val baseDir = ConfigFactory.load().getString("clustering.base-dir")

    new File(baseDir).mkdirs

    args.headOption match {

      case None =>
        startClusterInSameJvm()

      case Some(portString) if portString.matches("""\d+""") =>
        val port = portString.toInt
        if (masterPortRange.contains(port)) startMaster(port)
        else if (orchestratorPortRange.contains(port)) startOrchestrator(port)
        else if (apiActorPortRange.contains(port)) startApiServer(port, args.lift(1).map(_.toInt).getOrElse(8080))
        else startWorker(port, args.lift(1).map(_.toInt).getOrElse(1))

      case Some("cassandra") =>
        startCassandraDatabase()
        println("Started Cassandra, press Ctrl + C to kill")
        new CountDownLatch(1).await()

    }
  }

  def startClusterInSameJvm(): Unit = {
    startCassandraDatabase()

    startApiServer(6000, 8080)

    startMaster(2551)

    startOrchestrator(3001)

    startWorker(5001, 1, 1)
    startWorker(5002, 1, 2)
    //    startWorker(5003, 1, 3)
    //    startWorker(5004, 1, 4)
    //    startWorker(5005, 1, 8)
    //    startWorker(5006, 1, 9)
    //    startWorker(5007, 1, 10)
    //    startWorker(5008, 1, 10)
    //    startWorker(5009, 1, 10)
    //    startWorker(5010, 1, 10)
  }

  /**
    * Start a node with role api on the given port.
    */
  def startApiServer(actorPort: Int, serverPort: Int): Unit = {
    implicit val system: ActorSystem = ActorSystem("ClusterSystem", config(actorPort, "api-server"))
    val serverActor: ActorRef = system.actorOf(Services.props, "api-server")

    new ServerStart(serverPort, serverActor, system)
  }

  /**
    * Start a node with the role master on the given port.
    */

  def startMaster(port: Int): Unit = {
    val system = ActorSystem("ClusterSystem", config(port, "master"))
    MasterSingleton.startSingleton(system)
  }


  /**
    * Start a orchestrator node that will submit work to the master nodes
    */
  def startOrchestrator(port: Int): Unit = {
    val system = ActorSystem("ClusterSystem", config(port, "orchestrator"))
    system.actorOf(Orchestrator.props, "orchestrator")
  }


  /**
    * Start a worker node, with n actual workers that will accept and process workloads
    */
  def startWorker(port: Int, workers: Int, node: Int = 0): Unit = {
    val system = ActorSystem("ClusterSystem", config(port, "worker"))
    val masterProxy = system.actorOf(
      MasterSingleton.proxyProps(system),
      name = "masterProxy")

    (1 to workers).foreach(n =>
      system.actorOf(Worker.props(masterProxy), s"worker-$node-$n")
    )
  }


  def config(port: Int, role: String): Config =
    ConfigFactory.parseString(
      s"""
      akka.remote.netty.tcp.port=$port
      akka.cluster.roles=[$role]
    """).withFallback(ConfigFactory.load())

  /**
    * To make the sample easier to run we kickstart a Cassandra instance to
    * act as the journal. Cassandra is a great choice of backend for Akka Persistence but
    * in a real application a pre-existing Cassandra cluster should be used.
    */
  def startCassandraDatabase(): Unit = {
    val databaseDirectory = new File("target/cassandra-db")
    CassandraLauncher.start(
      databaseDirectory,
      CassandraLauncher.DefaultTestConfigResource,
      clean = true,
      port = 9042
    )

    // shut the cassandra instance down when the JVM stops
    sys.addShutdownHook {
      CassandraLauncher.stop()
    }
  }

}
