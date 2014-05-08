package se.jaklec.pwmc.read

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import GpioScheduler.Tick
import se.jaklec.rpi.gpio.Gpio.{Off, On}
import se.jaklec.rpi.gpio.Gpio
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import scala.util.Success
import se.jaklec.pwmc.PowmonSpec

class GpioReaderSpec
  extends TestKit(ActorSystem("GpioReaderSpec"))
  with ImplicitSender
  with PowmonSpec
  with MockitoSugar {

  val gpio = mock[Gpio]
  val reader = system.actorOf(Props(new GpioReader(gpio)))

  override def afterAll() {
    system.shutdown()
  }

  "The GpioReader" should {

    "read digital signal when it receives a Tick" in {

      when(gpio.readDigital) thenReturn Success(On)
      reader ! Tick
      expectMsg(On)
    }

    "only propagate state changes" in {

      when(gpio.readDigital)
        .thenReturn(Success(Off))
        .thenReturn(Success(On))
        .thenReturn(Success(Off))
        .thenReturn(Success(Off))
        .thenReturn(Success(Off))
        .thenReturn(Success(On))

      reader ! Tick
      reader ! Tick
      reader ! Tick
      reader ! Tick
      reader ! Tick
      reader ! Tick

      expectMsg(Off)
      expectMsg(On)
      expectMsg(Off)
      expectMsg(On)
    }
  }
}
