package se.jaklec.pwmc.read

import akka.actor.{ActorLogging, Actor}
import se.jaklec.rpi.gpio.Gpio
import GpioSupervisor.Tick
import se.jaklec.rpi.gpio.Gpio._
import scala.concurrent.duration._
import akka.pattern.CircuitBreaker
import scala.language.postfixOps

object GpioReader {
  def apply() = new GpioReader(Pin0) with GpioBreakerConfig
}

trait GpioBreakerConfig {
  val maxFailures = 3
  val callTimeout = 50 millis
  val resetTimeout = 1 minute
}

class GpioReader(gpio: Gpio) extends Actor with ActorLogging {
  this: GpioBreakerConfig =>

  implicit val ec = context.dispatcher

  lazy val breaker = new CircuitBreaker(context.system.scheduler, maxFailures, callTimeout, resetTimeout)
    .onClose(log info("Closing circuit breaker."))


  override def preStart(): Unit = gpio open In
  override def postStop(): Unit = gpio close

  def receive = {
    case Tick =>
      val s = sender
      breaker.onOpen {
        log warning("Cannot read from GPIO. Circuit breaker is open.")
        s ! ServiceUnavailable
      }.withCircuitBreaker(gpio.asyncReadDigital).onSuccess {
        case d: Digital =>
          s ! d
      }
  }
}
