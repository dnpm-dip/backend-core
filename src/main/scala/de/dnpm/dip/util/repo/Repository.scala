package de.dnpm.dip.util.repo


import cats.Monad


trait Repository[F[_],T,Id]
{

  def save(
    t: T
  )(
    implicit F: Monad[F]
  ): F[T]


  def get(
    id: Id
  )(
    implicit F: Monad[F]
   ): F[Option[T]]


  def update(
    id: Id,
    up: T => T
  )(
    implicit F: Monad[F]
  ): F[Option[T]]

  def updateWhere(
    p: T => Boolean,
    up: T => T
  )(
    implicit F: Monad[F]
  ): F[Iterable[T]]


  def query(
    p: T => Boolean
  )(
    implicit F: Monad[F]
  ): F[Iterable[T]]


  def delete(
    id: Id
  )(
    implicit F: Monad[F]
  ): F[Option[T]]

  def deleteWhere(
    p: T => Boolean
  )(
    implicit F: Monad[F]
  ): F[Iterable[T]]

}
