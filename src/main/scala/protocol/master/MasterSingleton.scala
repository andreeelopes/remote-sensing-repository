package protocol.master

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.cluster.singleton.{ClusterSingletonManager, ClusterSingletonManagerSettings, ClusterSingletonProxy, ClusterSingletonProxySettings}

import scala.concurrent.duration._


object MasterSingleton {

  private val singletonName = "master"
  private val singletonRole = "master"

  // #singleton
  def startSingleton(system: ActorSystem): ActorRef = {
    val workTimeout = system.settings.config.getDuration("distributed-workers.clean-up-timeout").getSeconds.seconds

    system.actorOf(
      ClusterSingletonManager.props(
        Master.props(workTimeout),
        PoisonPill,
        ClusterSingletonManagerSettings(system).withRole(singletonRole)
      ),
      singletonName)
  }

  // #singleton

  // #proxy
  def proxyProps(system: ActorSystem): Props = ClusterSingletonProxy.props(
    settings = ClusterSingletonProxySettings(system).withRole(singletonRole),
    singletonManagerPath = s"/user/$singletonName")

  // #proxy
}
