package spores.sample

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import spores.{Duplicable, Duplicate}
import spores.Duplicate.given


object Futures:
  given [T]: Duplicable[Future[T]] with
    def duplicate(fut: Future[T]): Future[T] = fut

  /**
    * Computes the nth Fibonacci number.
    *
    * Recursively computes the (n-1)th and (n-2)th
    * Fibonacci numbers concurrently using futures.
    */
  def fib(n: Int): Future[Int] =
    if (n <= 2) then
      Future.successful(1)
    else
      val fut1 = fib(n - 1)
      val fut2 = fib(n - 2)

      // captured variables are passed explicitly to
      // `apply` method of `Block` object
      fut1.flatMap(
        Duplicate.applyWithEnv(fut2) { fut2 => (res1: Int) =>
          fut2.map(
            Duplicate.applyWithEnv(res1) {
              res1 => (res2: Int) => res1 + res2
            }.unwrap()
          )
        }.unwrap()
      )

  def main(args: Array[String]): Unit =
    // computes 8th Fibonacci number = 21
    val res = Await.result(fib(8), Duration(10, "sec"))
    assert(res == 21)
    println(res)
