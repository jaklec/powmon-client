package se.jaklec.pwmc.core

import akka.actor.{Props, ActorSystem}
import se.jaklec.pwmc.read.{SupervisionStrategy, Transmitter, GpioReader, GpioSupervisor}

trait Core {
  implicit def system: ActorSystem
}

trait BootedCore extends Core {
  implicit lazy val system = ActorSystem("pwr-mtx-actor-system")

  sys.addShutdownHook(system.shutdown())
}

trait CoreActors {
  this: Core =>

  val reader = system.actorOf(Props(GpioReader()), "gpio-reader")
  val feeder = system.actorOf(Props(Transmitter()), "feeder")
  system.actorOf(Props(new GpioSupervisor(reader, feeder) with SupervisionStrategy), "supervisor")
}

object Boot extends App with BootedCore with CoreActors {
  println("Starting Power Monitor Client")
}
