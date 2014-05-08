package se.jaklec.pwmc.read

import akka.actor.{ActorLogging, Actor}
import se.jaklec.rpi.gpio.Gpio
import GpioSupervisor.Tick
import se.jaklec.rpi.gpio.Gpio._
import scala.util.{Success, Failure}

object GpioReader {
  def apply() = new GpioReader(Pin0)
}

class GpioReader(gpio: Gpio) extends Actor with ActorLogging {

  def readDigital = {
    try {
      gpio.readDigital match {
        case s: Success[Digital] => s.get
        case f@Failure(_) => sender ! NotADigitalValue
      }
    } catch {
      case e: Exception =>
        sender ! ServiceUnavailable
    }
  }

  def receive = {
    case Tick =>
      readDigital match {
        case v@On => sendOn(v)
        case v@Off => sendOff(v)
      }
  }

  def on: Receive = {
    case Tick =>
      readDigital match {
        case v@Off => sendOff(v)
        case _ =>
      }
  }

  def off: Receive = {
    case Tick =>
      readDigital match {
        case v@On => sendOn(v)
        case _ =>
      }
  }

  def sendOff(v: Off.type) {
    sender ! v
    context become off
  }

  def sendOn(v: On.type) {
    sender ! v
    context become on
  }
}
