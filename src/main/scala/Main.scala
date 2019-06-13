

import java.io.File
import java.util.concurrent.CountDownLatch

import akka.actor.ActorSystem
import akka.persistence.cassandra.testkit.CassandraLauncher
import com.typesafe.config.{Config, ConfigFactory}
import protocol.master.MasterSingleton
import protocol.scheduler.Orchestrator
import protocol.worker.Worker


object Main {

  // note that 2551 and 2552 are expected to be seed nodes though, even if
  // the master starts at 2000
  val masterPortRange = 2000 to 2999

  val orchestratorPortRange = 3000 to 3999

  def main(args: Array[String]): Unit = {

    new File("data").mkdirs //TODO

    args.headOption match {

      case None =>
        startClusterInSameJvm()

      case Some(portString) if portString.matches("""\d+""") =>
        val port = portString.toInt
        if (masterPortRange.contains(port)) startMaster(port)
        else if (orchestratorPortRange.contains(port)) startOrchestrator(port)
        else startWorker(port, args.lift(1).map(_.toInt).getOrElse(1))

      case Some("cassandra") =>
        startCassandraDatabase()
        println("Started Cassandra, press Ctrl + C to kill")
        new CountDownLatch(1).await()

    }
  }

  def startClusterInSameJvm(): Unit = {
    startCassandraDatabase()

    // backend nodes
    startMaster(2551)
    //    startBackEnd(2552)
    // orchestrator nodes
    //    startFrontEnd(3000)
    startOrchestrator(3001)
    // worker nodes
    startWorker(5001, 1, 1)
    //    startWorker(5002, 1, 2)
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
    * Start a node with the role backend on the given port. (This may also
    * start the shared journal, see below for details)
    */
  def startMaster(port: Int): Unit = {
    val system = ActorSystem("ClusterSystem", config(port, "master"))
    MasterSingleton.startSingleton(system)
  }

  /**
    * Start a front end node that will submit work to the backend nodes
    */
  // #orchestrator
  def startOrchestrator(port: Int): Unit = {
    val system = ActorSystem("ClusterSystem", config(port, "orchestrator"))
    system.actorOf(Orchestrator.props, "orchestrator")
    //    system.actorOf(WorkResultConsumer.props, "consumer")
  }

  // #orchestrator

  /**
    * Start a worker node, with n actual workers that will accept and process workloads
    */
  // #worker
  def startWorker(port: Int, workers: Int, node: Int = 0): Unit = {
    val system = ActorSystem("ClusterSystem", config(port, "worker"))
    val masterProxy = system.actorOf(
      MasterSingleton.proxyProps(system),
      name = "masterProxy")

    (1 to workers).foreach(n =>
      system.actorOf(Worker.props(masterProxy), s"worker-$node-$n")
    )
  }

  // #worker

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
