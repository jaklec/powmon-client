package se.jaklec.pwrmtx.client

import akka.actor.{Props, ActorSystem}

trait Core {
  implicit def system: ActorSystem
}

trait BootedCore extends Core {
  implicit lazy val system = ActorSystem("pwr-mtx-actor-system")

  sys.addShutdownHook(system.shutdown())
}

trait CoreActors {
  this: Core =>

  val gpioReader = system.actorOf(Props[Dispatcher], "gpio-reader")
}