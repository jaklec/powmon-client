package se.jaklec.pwmc.read

import akka.actor.Actor
import scala.concurrent.duration._

object GpioSupervisor {
  case object Tick
}

trait GpioSupervisor extends Actor {
  import GpioSupervisor._

  implicit val ec = context.dispatcher

  val ticker = context.system.scheduler.schedule( 100.millis, 100.millis, self, Tick)
}
