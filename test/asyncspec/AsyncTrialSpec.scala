package asyncspec

import asyncspec.Time.time
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.specs2.mock.Mockito

import scala.concurrent.Future


class ScalaFuturesSpec extends FlatSpec with Mockito with Matchers with ScalaFutures {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit override val patienceConfig: PatienceConfig =
    PatienceConfig(timeout = Span(50, Seconds), interval = Span(20, Millis))

  def aTask = Future {
    Thread.sleep(5000)
  }


  "first" should s"run as long as it takes" in {


    val task = aTask

    whenReady(task) { _ =>
      println("first task done")
      1 shouldBe 1
    }
  }


  "second" should s"run as long as it takes" in {


    val task = aTask

    whenReady(task) { _ =>
      println("second task done")
      1 shouldBe 1
    }
  }
}

class AsyncTrialSpec extends AsyncFlatSpec with ParallelTestExecution {

//  implicit override def executionContext = scala.concurrent.ExecutionContext.Implicits.global
  
  def aTask = Future {
    Thread.sleep(5000)
  }


  "first" should s"run as long as it takes" in {


    val task = aTask

    task.map { _ =>
      println("first task done")
      assert(1 == 1)
    }
  }

  "second" should s"run as long as it takes" in {


    val task = aTask

    task.map { _ =>
      println("second task done")
      assert(1 == 1)
    }
  }
}


object Time {


  def time[R](block: => R): R = {
    val t0 = System.currentTimeMillis()
    val result = block // call-by-name
    val t1 = System.currentTimeMillis()
    println("Elapsed time: " + (t1 - t0) + "ms")
    result
  }
}

