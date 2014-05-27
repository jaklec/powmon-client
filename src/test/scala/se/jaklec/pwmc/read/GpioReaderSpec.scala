package se.jaklec.pwmc.read

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import GpioSupervisor.Tick
import se.jaklec.rpi.gpio.Gpio.{Digital, Off, On}
import se.jaklec.rpi.gpio.{ReadException, Gpio}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import scala.util.{Failure, Success}
import scala.concurrent.{Future, Await, future}
import scala.concurrent.duration._
import se.jaklec.pwmc.PowmonSpec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps

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

    "read digital signal asynchronously when it receives a Tick" in {

      val f = future { On }
      when(gpio.asyncReadDigital) thenReturn f
      checkMsg(f, On)
    }

    "read multiple signals of Off and On" in {

      val f0 = future { Off }
      val f1 = future { On }
      val f2 = future { Off }
      val f3 = future { Off }
      val f4 = future { On }

      when(gpio.asyncReadDigital) thenReturn(f0, f1, f2, f3, f4)

      checkMsg(f0, Off)
      checkMsg(f1, On)
      checkMsg(f2, Off)
      checkMsg(f3, Off)
      checkMsg(f4, On)
    }

    "not propagate failures" in {

      val f = future { throw new ReadException("fatal error") }
      when(gpio.asyncReadDigital) thenReturn f

      reader ! Tick
      Await.ready(f, 5 millis)
      expectNoMsg(5 millis)
    }


    def checkMsg(f: Future[Digital], em: Digital) {
      reader ! Tick
      Await.ready(f, 5 millis)
      expectMsg(em)
    }


    //        .thenReturn(Success(On))
    //        .thenReturn(Success(Off))
    //      when(gpio.readDigital)
    //
    //    "only propagate state changes" in {
    //
    //        .thenReturn(Success(Off))
//        .thenReturn(Success(Off))
//        .thenReturn(Success(Off))
//        .thenReturn(Success(On))
//
//      reader ! Tick
//      reader ! Tick
//      reader ! Tick
//      reader ! Tick
//      reader ! Tick
//      reader ! Tick
//
//      expectMsg(Off)
//      expectMsg(On)
//      expectMsg(Off)
//      expectMsg(On)
//    }
//
//    "inform supervisor about analog reads" in {
//
//      when(gpio.readDigital) thenReturn Failure(new Exception("test failure"))
//
//      reader ! Tick
//      reader ! Tick
//      reader ! Tick
//
//      expectMsg(NotADigitalValue)
//      expectMsg(NotADigitalValue)
//      expectMsg(NotADigitalValue)
//    }
//
//    "inform supervisor after real failures" in {
//
//      when(gpio.readDigital) thenThrow new RuntimeException("Big problem with the GPIO")
//
//      reader ! Tick
//      reader ! Tick
//      reader ! Tick
//
//      expectMsg(ServiceUnavailable)
//      expectMsg(ServiceUnavailable)
//      expectMsg(ServiceUnavailable)
//    }
  }
}
