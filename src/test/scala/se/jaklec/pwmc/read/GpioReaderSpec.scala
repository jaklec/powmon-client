package se.jaklec.pwmc.read

import akka.testkit.{TestActorRef, ImplicitSender, TestKit}
import akka.actor.{Props, ActorRef, ActorSystem}
import GpioSupervisor.Tick
import se.jaklec.rpi.gpio.Gpio._
import se.jaklec.rpi.gpio.{ReadException, Gpio}
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import scala.concurrent.{Future, Await, future}
import scala.concurrent.duration._
import se.jaklec.pwmc.PowmonSpec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.BeforeAndAfterEach

class GpioReaderSpec extends TestKit(ActorSystem("GpioReaderSpec"))
  with ImplicitSender with PowmonSpec with MockitoSugar with ScalaFutures {

  trait GpioBreakerTestConfig extends GpioBreakerConfig {
    override val maxFailures = 2
    override val callTimeout = 1 millis
    override val resetTimeout = 3 millis
  }

  override protected def afterAll(): Unit = system.shutdown()

  "A GpioReader" should {

    "read digital signal asynchronously when it receives a Tick" in {
      val gpio = mock[Gpio]
      val reader = TestActorRef[GpioReader](Props(new GpioReader(gpio) with GpioBreakerTestConfig))
      val f = future { On }
      when(gpio.asyncReadDigital) thenReturn f
      checkMsg(reader, f, On)
    }

    "read multiple signals of Off and On" in {
      val gpio = mock[Gpio]
      val reader = TestActorRef[GpioReader](Props(new GpioReader(gpio) with GpioBreakerTestConfig))

      val f0 = future { Off }
      val f1 = future { On }
      val f2 = future { Off }
      val f3 = future { Off }
      val f4 = future { On }

      when(gpio.asyncReadDigital) thenReturn(f0, f1, f2, f3, f4)

      checkMsg(reader, f0, Off)
      checkMsg(reader, f1, On)
      checkMsg(reader, f2, Off)
      checkMsg(reader, f3, Off)
      checkMsg(reader, f4, On)
    }

    "not propagate failures" in {
      val gpio = mock[Gpio]
      val reader = TestActorRef[GpioReader](Props(new GpioReader(gpio) with GpioBreakerTestConfig))

      val f = future { throw new ReadException("fatal error") }
      when(gpio.asyncReadDigital) thenReturn f

      reader ! Tick
      Await.ready(f, 5 millis)
      expectNoMsg(5 millis)
    }

    "open circuit breaker when failing repeatedly" in {
      val gpio = mock[Gpio]
      val reader = TestActorRef[GpioReader](Props(new GpioReader(gpio) with GpioBreakerTestConfig))

      val f0 = future { throw new ReadException("fatal error") }
      val f1 = future { throw new ReadException("fatal error") }
      val f2 = future { throw new ReadException("fatal error") }

      when(gpio.asyncReadDigital) thenReturn(f0, f1, f2)

      reader ! Tick
      Await.ready(f0, 5 millis)
      expectNoMsg(5 millis)

      reader ! Tick
      Await.ready(f1, 5 millis)
      expectMsg(5 millis, ServiceUnavailable)

      reader ! Tick
      Await.ready(f2, 5 millis)
      expectMsg(5 millis, ServiceUnavailable)
    }

    def checkMsg(reader: ActorRef, f: Future[Digital], em: Digital) {
      reader ! Tick
      Await.ready(f, 5 millis)
      expectMsg(em)
    }
  }

  "An uninitialized GpioReader" should {

    "open port after start" in {
      val gpio = mock[Gpio]
      TestActorRef[GpioReader](Props(new GpioReader(gpio) with GpioBreakerTestConfig))
      verify(gpio) open In
    }
  }

  "An initialized GpioReader" should {

    "close port before stop" in {
      val gpio = mock[Gpio]
      val reader = TestActorRef[GpioReader](Props(new GpioReader(gpio) with GpioBreakerTestConfig))
      reader.stop()
      verify(gpio) close
    }
  }
}
