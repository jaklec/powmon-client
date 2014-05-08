package se.jaklec.pwmc.read

import akka.actor.{ActorLogging, Actor}
import se.jaklec.rpi.gpio.Gpio
import GpioScheduler.Tick
import se.jaklec.rpi.gpio.Gpio._

object GpioReader {
  def apply() = new GpioReader(Pin0)
}

class GpioReader(gpio: Gpio) extends Actor with ActorLogging {

  def receive = {
    case Tick =>
      readDigital match {
        case v @ On => sendOn(v)
        case v @ Off => sendOff(v)
      }
  }

  def on: Receive = {
    case Tick =>
      readDigital match {
        case v @ Off => sendOff(v)
        case _ =>
      }
  }

  def off: Receive = {
    case Tick =>
      readDigital match {
        case v @ On => sendOn(v)
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

  def readDigital: Digital = gpio.readDigital.get
}
