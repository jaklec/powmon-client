package se.jaklec.pwrmtx.client

import akka.actor.{ActorLogging, Actor}
import se.jaklec.rpi.gpio.Gpio
import se.jaklec.pwrmtx.client.Dispatcher.Tick
import se.jaklec.rpi.gpio.Gpio.{Value, Pin0}
import scala.util.Try

object GpioReader {
  def apply() = new GpioReader(Pin0)
}

class GpioReader(gpio: Gpio) extends Actor with ActorLogging {

  def receive = {
    case Tick =>
      val res: Try[Value] = gpio.readDigital
      sender ! res.get
  }
}
