package de.dnpm.dip.util



object Operations
{

  def patch[T](
    t: T
  )(
    patches: Option[T => T]*
  ): T = {

    patches.foldLeft(
      t
    )(
      (tpr,p) => p.fold(tpr)(f => f(tpr))
    )

  }


  def update[T](
    t: T
  )(
    updates: (T => T)*
  ): T = {

    updates.foldLeft(
      t
    )(
      (tpr,f) => f(tpr)
    )

  }


  object syntax {

    implicit class OperationSyntax[T](val t: T) extends AnyVal
    {
      def patch(patches: Option[T => T]*): T =
        Operations.patch(t)(patches: _*)

      def update(updates: T => T*): T =
        Operations.update(t)(updates: _*)
    }


  }


}
