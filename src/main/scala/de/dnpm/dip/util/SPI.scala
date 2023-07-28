package de.dnpm.dip.util


import java.util.ServiceLoader

import scala.util.{
  Try,
  Failure
}
import scala.reflect.ClassTag
import scala.collection.Factory


trait ServiceProviderInterface
{
  type Service

  def getInstance: Service

}

trait SPI[T] extends ServiceProviderInterface
{
  type Service = T
}


abstract class SPILoader[S <: ServiceProviderInterface]
(
  implicit val spi: ClassTag[S]
)
extends Logging
{

  def getInstance: Try[S#Service] =
    Try {
      ServiceLoader.load(spi.runtimeClass.asInstanceOf[Class[S]])
        .iterator
        .next
    }
    .recoverWith {
      case t =>
        log.warn(
          s"""Failed to load Service Provider Interface instance for ${spi.runtimeClass.getName}.
          Unless handled properly with a fallback option in the client component, this is the cause of any occurring exception!"""
        )
        Failure(t)
    }
    .map(_.getInstance)

  def getInstances: Iterator[S#Service] = {

    import scala.jdk.CollectionConverters._

    ServiceLoader.load(spi.runtimeClass.asInstanceOf[Class[S]])
      .iterator
      .asScala
      .map(_.getInstance)

  }

}


trait ServiceProviderInterfaceF
{
  self =>

  type Service[F[_]]

  def getInstance[F[_]]: Service[F]

}


trait SPIF[S[F[_]]] extends ServiceProviderInterfaceF
{
  type Service[M[_]] = S[M]
}


abstract class SPILoaderF[S <: ServiceProviderInterfaceF]
(
  implicit val spi: ClassTag[S]
)
extends Logging
{

  def getInstance[F[_]]: Try[S#Service[F]] =
    Try {
      ServiceLoader.load(spi.runtimeClass.asInstanceOf[Class[S]])
        .iterator
        .next
    }
    .recoverWith {
      case t =>
        log.warn(
          s"""Failed to load Service Provider Interface instance for ${spi.runtimeClass.getName}.
          Unless handled properly with a fallback option in the client component, this is the cause of any occurring exception!"""
        )
        Failure(t)
    }
    .map(_.getInstance[F])


  def getInstances[F[_]]: Iterator[S#Service[F]] = {

    import scala.jdk.CollectionConverters._

    ServiceLoader.load(spi.runtimeClass.asInstanceOf[Class[S]])
      .iterator
      .asScala
      .map(_.getInstance[F])

  }

}

