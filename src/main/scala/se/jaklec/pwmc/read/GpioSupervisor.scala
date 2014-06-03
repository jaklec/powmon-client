package se.jaklec.pwmc.read

import akka.actor.{ActorRef, Actor}
import scala.concurrent.duration._
import se.jaklec.rpi.gpio.Gpio.{On, Digital, Off}

object GpioSupervisor {
  case object Tick
}

trait SupervisionStrategy {
  import scala.language.postfixOps

  lazy val freq: FiniteDuration = 50 millis
}

class GpioSupervisor(gpioReader: ActorRef, feeder: ActorRef) extends Actor {
  this: SupervisionStrategy =>
  import GpioSupervisor._

  implicit val ec = context.dispatcher

  val ticker = context.system.scheduler.schedule(freq, freq, self, Tick)

  override def receive: Receive = {
    case Tick =>
      gpioReader ! Tick
    case o@On =>
      feeder ! o
      context become on
    case o@Off =>
      feeder ! o
      context become off
    case _ =>
  }

  def on: Receive = {
    case Tick =>
      gpioReader ! Tick
    case d@Off =>
      feeder ! d
      context become off
    case _ =>
  }

  def off: Receive = {
    case Tick =>
      gpioReader ! Tick
    case d@On =>
      feeder ! d
      context become on
    case _ =>
  }
}
