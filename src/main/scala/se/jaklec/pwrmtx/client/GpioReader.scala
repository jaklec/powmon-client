package se.jaklec.pwrmtx.client

import akka.actor.{ActorLogging, Actor}
import se.jaklec.rpi.gpio.Gpio
import se.jaklec.pwrmtx.client.Dispatcher.Tick
import se.jaklec.rpi.gpio.Gpio.{Off, On, Value, Pin0}
import scala.util.Try

object GpioReader {
  def apply() = new GpioReader(Pin0)
}

class GpioReader(gpio: Gpio) extends Actor with ActorLogging {

  def receive = {
    case Tick =>
      val res: Try[Value] = gpio.readDigital
      res.getOrElse() match {
        case v @ On =>
          sender ! v
          context become on
        case v @ Off =>
          sender ! v
          context become off
      }
  }

  def on: Receive = {
    case Tick =>
      val res: Try[Value] = gpio.readDigital
      res.getOrElse() match {
        case v @ Off =>
          sender ! v
          context become off
        case _ =>
      }
  }

  def off: Receive = {
    case Tick =>
      val res: Try[Value] = gpio.readDigital
      res.getOrElse() match {
        case v @ On =>
          sender ! v
          context become on
        case _ =>
      }
  }
}
