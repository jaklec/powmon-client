package se.jaklec.pwrmtx.client

import akka.actor.Actor
import scala.concurrent.duration._

object Dispatcher {
  case object Tick
}

trait Dispatcher extends Actor {
  import Dispatcher._

  implicit val ec = context.dispatcher

  val ticker = context.system.scheduler.schedule( 100.millis, 100.millis, self, Tick)
}
