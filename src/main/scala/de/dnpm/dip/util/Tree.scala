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
        _.view.flatMap(_.find(f)).headOption
      )

  def map[U](f: T => U): Tree[U] =
    Tree(
      f(element),
      children.map(_.map(_.map(f)))
    )

/*
  def find(f: T => Boolean): Option[T] = {

    @annotation.tailrec
    def findRecursive(seq: Seq[Tree[T]]): Option[T] = {
      if (seq.nonEmpty){
        val child = 
          seq.head.find(f)

        if (child.isDefined) child
        else findRecursive(seq.tail)
      } else
        None
    }

    if (f(element)) Some(element)
    else children.flatMap(findRecursive)
  }
*/

/*
  def filter(f: T => Boolean): Seq[T] = {
    Seq(element).filter(f) ++
      children.getOrElse(Seq.empty).flatMap(_.filter(f))
  }
*/
/*
  def foldLeft[U](z: U)(f: (U,T) => U): U = {
    val u = f(z,element)
    children.fold(
      u
    )(
      _.foldLeft(u)((acc,t) => t.foldLeft(acc)(f))
    )  
  }

  def filter(f: T => Boolean): Seq[T] = {
    foldLeft(Seq.empty[T])(
      (acc,t) =>
        if (f(t)) acc :+ t
        else acc
    )
  }
*/

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
