package de.dnpm.dip.util



import play.api.libs.json.{
  Json,
  JsPath,
  JsObject,
  Reads,
  OWrites
}


final case class Tree[+T]
(
  element: T,
  children: Option[Seq[Tree[T]]] = None
)
{
  def contains[Tpr >: T](t: Tpr): Boolean =
    element == t || children.exists(_.exists(_ contains t))

  def exists(f: T => Boolean): Boolean =
    f(element) || children.exists(_.exists(_.exists(f)))

  def find(f: T => Boolean): Option[T] =
    if (f(element))
      Some(element)
    else
      children.flatMap(
        _.flatMap(_.find(f)).headOption
      )

  def map[U](f: T => U): Tree[U] =
    Tree(
      f(element),
      children.map(_.map(_.map(f)))
    )
}


object Tree
{

  import play.api.libs.functional.syntax._


  // Custom Reads/Writes so that the JSON representation of a Tree[T]
  // is identical to T's JSON representation with a meta-data field "_children" 

  implicit def writesTree[T: OWrites]: OWrites[Tree[T]] =
    (
      JsPath.write[T] and
      (JsPath \ "_children").lazyWriteNullable(OWrites.seq[Tree[T]])
    )(
      unlift(Tree.unapply[T](_))
    )

  implicit def readsTree[T: Reads: OWrites]: Reads[Tree[T]] =
    (
      JsPath.read[T] and
      (JsPath \ "_children").lazyReadNullable(Reads.seq[Tree[T]])
    )(
      Tree(_,_)
    )

}
