package se.jaklec.pwmc.read

import akka.actor.Actor
import scala.concurrent.duration._

object GpioScheduler {
  case object Tick
}

trait GpioScheduler extends Actor {
  import GpioScheduler._

  implicit val ec = context.dispatcher

  val ticker = context.system.scheduler.schedule( 100.millis, 100.millis, self, Tick)
}
