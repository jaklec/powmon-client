package se.jaklec.pwrmtx.client

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import se.jaklec.pwrmtx.client.Dispatcher.Tick
import se.jaklec.rpi.gpio.Gpio.On
import se.jaklec.rpi.gpio.Gpio
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import scala.util.Success

class GpioReaderSpec
  extends TestKit(ActorSystem("GpioReaderSpec"))
  with ImplicitSender
  with PwrMtxSpec
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
  }
}