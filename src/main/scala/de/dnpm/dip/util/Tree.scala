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

  def addChild[U >: T](t: Tree[U]): Tree[U] =
    Tree(
      element,
      Some(children.fold(Seq(t))(_ :+ t))
    )


  def contains[Tpr >: T](t: Tpr): Boolean =
    exists(_ == t)


  def depth: Int =
    1 + children.flatMap(_.map(_.depth).maxOption).getOrElse(0)


  def exists(f: T => Boolean): Boolean =
    f(element) || children.exists(_.exists(_.exists(f)))


  def find(f: T => Boolean): Option[T] =
    Option.when(f(element))(element)
      .orElse(
        children.flatMap(
          _.foldLeft(Option.empty[T])((acc,ch) => acc orElse ch.find(f))
        )
      )

/*
  def find(f: T => Boolean): Option[T] =
    Option.when(f(element))(element)
      .orElse(
        children.flatMap(
          _.view.flatMap(_.find(f)).headOption
        )
      )
*/


  def foldLeft[U](z: U)(f: (U,T) => U): U = {
    val u = f(z,element)
    children.fold(u)(_.foldLeft(u)((acc,t) => t.foldLeft(acc)(f)))  
  }


  def hasChildren: Boolean =
    children.exists(_.nonEmpty)


  def map[U](f: T => U): Tree[U] =
    Tree(
      f(element),
      children.map(_.map(_.map(f)))
    )


  def size: Int =
    1 + children.fold(0)(_.map(_.size).sum)


  def toSeq: Seq[T] =
    element +: children.getOrElse(Seq.empty[Tree[T]]).flatMap(_.toSeq) 


  def toSet[U >: T]: Set[U] =
    children.getOrElse(Seq.empty[Tree[T]]).flatMap(_.toSet).toSet + element

}


object Tree
{

  def apply[T](t: T, children: Tree[T]*): Tree[T] =
    Tree(
      t,
      Option(children).filter(_.nonEmpty)
    )


  type Expander[T] = T => Tree[T]

  object Expander
  {

    def apply[T](implicit exp: Expander[T]) = exp

    object syntax
    {
      implicit class ExpansionOps[T](val t: T) extends AnyVal
      {
        def expand(implicit exp: Expander[T]): Tree[T] = exp(t) 
      } 
    }

  }



  import play.api.libs.functional.syntax._


  // Custom Reads/Writes so that the JSON representation of a Tree[T]
  // is identical to T's JSON representation with a meta-data field "_children" 

  implicit def writesTree[T: OWrites]: OWrites[Tree[T]] =
    (
      JsPath.write[T] and
      (JsPath \ "children").lazyWriteNullable(OWrites.seq[Tree[T]])
    )(
      unlift(Tree.unapply[T](_))
    )

  implicit def readsTree[T: Reads: OWrites]: Reads[Tree[T]] =
    (
      JsPath.read[T] and
      (JsPath \ "children").lazyReadNullable(Reads.seq[Tree[T]])
    )(
      Tree(_,_)
    )


  import Completer.syntax._

  implicit def treeCompleter[T: Completer]: Completer[Tree[T]] =
    t => t.map(_.complete)

}
