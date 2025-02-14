package de.dnpm.dip.util


import java.util.concurrent.{
  Callable,
  ScheduledExecutorService
}
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger
import scala.util.Try
import cats.ApplicativeError
import cats.syntax.applicativeError._
import cats.syntax.functor._



class Retry[F[_],T,Err] private (
  private val task: () => F[T],
  private val name: String,
  private val strategy: Retry.Strategy,
  private val period: Int,
  private val executor: ScheduledExecutorService
)(
  implicit app: ApplicativeError[F,Err]
)
extends Runnable
with Logging
{

  private lazy val failedTries =
    new AtomicInteger(0)

  private val runnable = 
    new Runnable { override def run = attempt() }

  private def attempt(): Unit = {
    task()
      .map(_ => failedTries.set(0))
      .onError {
        case t =>
          strategy match {
            case Retry.UntilSucceeded =>
              log.error(s"Task '$name' failed, retrying in $period s", t)
              executor.schedule(
                runnable,
                period.toLong,
                SECONDS
              )
      
            case Retry.AtMost(n) => 
              if (failedTries.incrementAndGet <= n){
                log.error(s"Task '$name' failed, retrying in $period s", t)
                executor.schedule(
                  runnable,
                  period.toLong,
                  SECONDS
                )
              } else
                log.warn(
                  s"Task '$name' given up as permanent failure after ${failedTries.get - 1}/$n failed attempts" // Subtract 1 because last check above also increments
                ) 
              
          }
          app.unit
      }

    ()
  }

  override def run(): Unit =
    attempt()

  def cancel(): Unit =
    executor.shutdown

}


object Retry
{

  sealed trait Strategy

  final case class AtMost(n: Int) extends Strategy
  final case object UntilSucceeded extends Strategy


  def apply[F[_],T,Err](
    task: () => F[T],
    name: String,
    maxTries: Int,
    period: Int
  )(
    implicit
    executor: ScheduledExecutorService,
    app: ApplicativeError[F,Err]
  ): Retry[F,T,Err] =
    new Retry(
      task,
      name,
      AtMost(maxTries),
      period,
      executor
    )

  def apply[F[_],T,Err](
    task: () => F[T],
    name: String,
    period: Int
  )(
    implicit
    executor: ScheduledExecutorService,
    app: ApplicativeError[F,Err]
  ): Retry[F,T,Err] =
    new Retry(
      task,
      name,
      UntilSucceeded,
      period,
      executor
    )


  def apply[T](
    task: Callable[T],
    name: String,
    maxTries: Int,
    period: Int
  )(
    implicit
    executor: ScheduledExecutorService,
    app: ApplicativeError[Try,Throwable]
  ): Retry[Try,T,Throwable] =
    new Retry(
      () => Try { task.call },
      name,
      AtMost(maxTries),
      period,
      executor
    )

  def apply[T](
    task: Callable[T],
    name: String,
    period: Int
  )(
    implicit
    executor: ScheduledExecutorService,
    app: ApplicativeError[Try,Throwable]
  ): Retry[Try,T,Throwable] =
    new Retry(
      () => Try { task.call },
      name,
      UntilSucceeded,
      period,
      executor
    )

}


