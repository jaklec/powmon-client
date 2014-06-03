package se.jaklec.pwmc.read

import akka.testkit.{TestProbe, TestActorRef, ImplicitSender, TestKit}
import akka.actor.{Props, ActorSystem}
import se.jaklec.pwmc.PowmonSpec
import org.scalatest.mock.MockitoSugar
import scala.language.postfixOps
import scala.concurrent.duration._
import se.jaklec.pwmc.read.GpioSupervisor.Tick
import se.jaklec.rpi.gpio.Gpio.{Off, On}

class GpioSupervisorSpec extends TestKit(ActorSystem("GpioSupervisorSpec"))
  with ImplicitSender with PowmonSpec with MockitoSugar {



  trait TestSupervisionStrategy extends SupervisionStrategy {
    override lazy val freq: FiniteDuration = 3 nanos
  }

  override protected def afterAll(): Unit = system.shutdown()

  "A GpioSupervisor" should {

    "schedule multiple reads" in {
      val p = TestProbe()
      TestActorRef(Props(new GpioSupervisor(p.ref, TestProbe().ref) with TestSupervisionStrategy))
      var ticks = 0
      p.fishForMessage(500 millis) {
        case Tick if (ticks > 1) => true
        case Tick => ticks += 1; false
        case _ => false
      }
    }

    "propagate digital reads" in {
      val p = TestProbe()
      val a = TestActorRef(Props(new GpioSupervisor(TestProbe().ref, p.ref) with TestSupervisionStrategy))
      a ! On
      p.expectMsg(50 nanos, On)
      a ! Off
      p.expectMsg(50 nanos, Off)
    }

    "not propagate repeats" in {
      val p = TestProbe()
      val a = TestActorRef(Props(new GpioSupervisor(TestProbe().ref, p.ref) with TestSupervisionStrategy))
      a ! On
      p.expectMsg(50 nanos, On)
      a ! On
      p.expectNoMsg(50 nanos)
      a ! Off
      p.expectMsg(50 nanos, Off)
      a ! Off
      p.expectNoMsg(50 nanos)
      a ! On
      p.expectMsg(50 nanos, On)
    }
  }
}
