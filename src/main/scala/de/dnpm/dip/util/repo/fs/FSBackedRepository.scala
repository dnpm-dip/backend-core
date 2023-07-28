package de.dnpm.dip.util.repo.fs


import java.io.{
  File,
  FileWriter,
  InputStream,
  IOException,
  FileInputStream
}
import java.nio.file.Files
import java.util.UUID.randomUUID
import scala.util.{
  Failure,
  Success
}
import scala.collection.concurrent.{
  Map,
  TrieMap
}
import play.api.libs.json.{
  Json,
  Format
}
import cats.Monad
import cats.MonadError
import cats.syntax.functor._
import cats.syntax.flatMap._
import de.dnpm.dip.util.Logging
import de.dnpm.dip.util.repo.Repository


sealed trait FSBackedRepository[F[_],T,Id]
  extends Repository[F,T,Id]


object FSBackedRepository
{

  // Ensure only one Repository instance is created for a given data dir
  private val repos: Map[File,Any] =
    TrieMap.empty[File,Any]

  def apply[F[_],T,Id](
    dataDir: File,
    prefix: String,
    cached: Boolean,
    idOf: T => Id,
    id2str: Id => String
  )(
    implicit
    f: Format[T]
  ): FSBackedRepository[F,T,Id] = {

    if (!dataDir.exists) dataDir.mkdirs

    val cache =
      if (cached)
        Some(
          dataDir.list
            .to(LazyList)
            .map(new File(dataDir,_))
            .map(toFileInputStream)
            .map(Json.parse)
            .map(Json.fromJson[T](_))
            .map(_.get)
            .map(t => idOf(t) -> t)
        )
      else None

    repos.getOrElseUpdate(
      dataDir,
      new Impl[F,T,Id](
        dataDir,
        prefix,
        idOf,
        id2str,
        cache.map(TrieMap.from)
      )
    )
    .asInstanceOf[FSBackedRepository[F,T,Id]]

  }


  private class Impl[F[_],T: Format,Id]
  (
    dataDir: File,
    prefix: String,
    idOf: T => Id,
    id2str: Id => String,
    cache: Option[Map[Id,T]]
  )
  extends FSBackedRepository[F,T,Id]
     with Logging
  {

    private def fileOf(
      id: Id
    ): File =
      new File(
        dataDir,
        s"${prefix}_${id2str(id)}.json"
      )

    
    override def save(
      t: T
    )(
      implicit F: Monad[F]
    ): F[T] = {
       
      for {
        js <-
          F.pure { Json.prettyPrint(Json.toJson(t)) }

        _ = {
              val fw = new FileWriter(fileOf(idOf(t)))
              fw.write(js.toString)
              fw.close
            }

        _ = cache.foreach(_ += idOf(t) -> t)

      } yield t

    }


    override def update(
      id: Id,
      f: T => T
    )(
      implicit F: Monad[F]
    ): F[Option[T]] = {

      for {
        opt <- get(id)
        updated = opt.map(f)
        result <- updated.map(save(_).map(Some(_))).getOrElse(F.pure(None))
      } yield result

    }

    override def updateWhere(
      p: T => Boolean,
      f: T => T
    )(
      implicit F: Monad[F]
    ): F[Iterable[T]] = {

      import cats.instances.list._
      import cats.syntax.traverse._

      for {
        ts <- this.query(p)
        txn = ts.map(f).map(save).toList
        updated <- txn.sequence
      } yield updated.to(LazyList)

    }
    

    override def get(
      id: Id
    )(
      implicit F: Monad[F]
    ): F[Option[T]] =
      F.pure {

        cache match {

          case None =>
            for {
              file <- Option(fileOf(id))
              if file.exists
              t = Json.fromJson[T](Json.parse(toFileInputStream(file)))
              _  = t.fold(err => log.error(err.toString), x => ())
              if t.isSuccess
            } yield t.get

          case Some(m) => m.get(id)
        }

      }


    override def query(
      pred: T => Boolean
    )(
      implicit F: Monad[F]
    ): F[Iterable[T]] =
      F.pure {

        cache match {

          case None =>
            dataDir.list
              .to(LazyList)
              .map(new File(dataDir,_))
              .map(toFileInputStream)
              .map(Json.parse)
              .map(Json.fromJson[T](_))
              .map(_.get)
              .filter(pred)

          case Some(m) => m.values.filter(pred)
        }
      }


    override def delete(
      id: Id
    )(
      implicit F: Monad[F]
    ): F[Option[T]] = { 

      F.pure {
        for {
          file <- Option(fileOf(id))

          if file.exists

          entry <- Json.fromJson[T](Json.parse(toFileInputStream(file))).asOpt

          _  = Files.delete(file.toPath)
           
          _ = cache.foreach(_ -= id)

        } yield entry
      }

    }


    override def deleteWhere(
      p: T => Boolean
    )(
      implicit F: Monad[F]
    ): F[Iterable[T]] = {

      for {
        ts <- this.query(p)
        _  = ts.foreach(t => delete(idOf(t)))
      } yield ts

    }

  }


  private def toFileInputStream(f: File): InputStream =
    new FileInputStream(f)


}
