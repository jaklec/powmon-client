package se.jaklec.pwmc.read

import akka.actor.{ActorLogging, Actor}
import scala.language.postfixOps
import se.jaklec.rpi.gpio.Gpio.Digital

object Transmitter {
  def apply() = new Transmitter
}

class Transmitter extends Actor with ActorLogging {
  override def receive: Receive = {
    case m: Digital => log info s"received message $m"
  }
}
