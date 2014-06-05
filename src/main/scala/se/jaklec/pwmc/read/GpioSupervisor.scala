package se.jaklec.pwmc.read

import akka.actor.{ActorRef, Actor}
import scala.concurrent.duration._
import se.jaklec.rpi.gpio.Gpio.{On, Off}

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

  def receive: Receive = read orElse enterOn orElse enterOff
  def stateOn: Receive = read orElse enterOff orElse doNothing
  def stateOff: Receive = read orElse enterOn orElse doNothing

  def read: Receive = {
    case tick@Tick =>
      gpioReader ! tick
  }
  
  def enterOn: Receive = {
    case on@On =>
      feeder ! on
      context become stateOn
  }
  
  def enterOff: Receive = {
    case off@Off =>
      feeder ! off
      context become stateOff
  }
  
  def doNothing: Receive = {
    case _ => 
  }
}
