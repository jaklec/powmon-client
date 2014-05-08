package se.jaklec.pwmc.core

import akka.actor.{Props, ActorSystem}
import se.jaklec.pwmc.read.GpioScheduler

trait Core {
  implicit def system: ActorSystem
}

trait BootedCore extends Core {
  implicit lazy val system = ActorSystem("pwr-mtx-actor-system")

  sys.addShutdownHook(system.shutdown())
}

trait CoreActors {
  this: Core =>

  val gpioReader = system.actorOf(Props[GpioScheduler], "gpio-reader")
}