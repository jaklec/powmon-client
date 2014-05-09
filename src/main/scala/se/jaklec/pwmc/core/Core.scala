package se.jaklec.pwmc.core

import akka.actor.{Props, ActorSystem}
import se.jaklec.pwmc.read.GpioSupervisor

trait Core {
  implicit def system: ActorSystem
}

trait BootedCore extends Core {
  implicit lazy val system = ActorSystem("pwr-mtx-actor-system")

  sys.addShutdownHook(system.shutdown())
}

trait CoreActors {
  this: Core =>

  val gpioReader = system.actorOf(Props[GpioSupervisor], "gpio-supervisor")
}

object Boot extends App with BootedCore with CoreActors {
  println("Starting Power Monitor Client")
}
