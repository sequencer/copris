package copris

import scala.collection._
import scala.collection.immutable.IndexedSeq
import scala.collection.immutable.SortedSet

/**
 * Abstract class of expressions.
 * [[copris.Term]]s and [[copris.Constraint]]s are expressions.
 */
abstract class Expr {
  /** Returns an iterator of occuring variables */
  def variables: Iterator[Var] = Iterator.empty
  /** Returns an iterator of occuring Boolean variables */
  def bools: Iterator[Bool] = Iterator.empty
}

/**
 * Abstract class of terms.
 *
 * Operators defined in this class create a new expression.
 * For example, `x + y` returns a new term `Add(x, y)`
 * when `x` and `y` are terms.
 */
sealed abstract class Term extends Expr {
  /** Returns [[copris.Neg]] of Term */
  def unary_- = Neg(this)
  /** Returns [[copris.Add]] of Terms */
  def + (x: Term) = Add(this, x)
  /** Returns [[copris.Add]] of Term with Int */
  def + (a: Int) = Add(this, Num(a))
  /** Returns [[copris.Sub]] of Terms */
  def - (x: Term) = Sub(this, x)
  /** Returns [[copris.Sub]] of Term with Int */
  def - (a: Int) = Sub(this, Num(a))
  /** Returns [[copris.Mul]] of Terms */
  def * (x: Term) = Mul(this, x)
  /** Returns [[copris.Mul]] of Term with Int */
  def * (a: Int) = Mul(this, Num(a))
  /** Returns [[copris.Div]] of Terms */
  def / (x: Term) = Div(this, x)
  /** Returns [[copris.Div]] of Term by Int */
  def / (a: Int) = Div(this, Num(a))
  /** Returns [[copris.Mod]] of Terms */
  def % (x: Term) = Mod(this, x)
  /** Returns [[copris.Mod]] of Term by Int */
  def % (a: Int) = Mod(this, Num(a))
  /** Returns [[copris.Max]] of Terms */
  def max (x: Term) = Max(this, x)
  /** Returns [[copris.Max]] of Term and Int */
  def max (a: Int) = Max(this, Num(a))
  /** Returns [[copris.Min]] of Terms */
  def min (x: Term) = Min(this, x)
  /** Returns [[copris.Min]] of Term and Int */
  def min (a: Int) = Min(this, Num(a))

  /** Returns [[copris.Eq]] of Terms */
  def === (x: Term) = Eq(this, x)
  /** Returns [[copris.Eq]] of Term and Int */
  def === (a: Int) = Eq(this, Num(a))
  /** Returns [[copris.Ne]] of Terms */
  @deprecated("use =/= instead", "2.2.0")
  def !== (x: Term) = Ne(this, x)
  /** Returns [[copris.Ne]] of Term and Int */
  @deprecated("use =/= instead", "2.2.0")
  def !== (a: Int) = Ne(this, Num(a))
  /** Returns [[copris.Ne]] of Terms */
  def =/= (x: Term) = Ne(this, x)
  /** Returns [[copris.Ne]] of Term and Int */
  def =/= (a: Int) = Ne(this, Num(a))
  /** Returns [[copris.Le]] of Terms */
  def <= (x: Term) = Le(this, x)
  /** Returns [[copris.Le]] of Term and Int */
  def <= (a: Int) = Le(this, Num(a))
  /** Returns [[copris.Lt]] of Terms */
  def < (x: Term) = Lt(this, x)
  /** Returns [[copris.Lt]] of Term and Int */
  def < (a: Int) = Lt(this, Num(a))
  /** Returns [[copris.Ge]] of Terms */
  def >= (x: Term) = Ge(this, x)
  /** Returns [[copris.Ge]] of Term and Int */
  def >= (a: Int) = Ge(this, Num(a))
  /** Returns [[copris.Gt]] of Terms */
  def > (x: Term) = Gt(this, x)
  /** Returns [[copris.Gt]] of Term and Int */
  def > (a: Int) = Gt(this, Num(a))
  /** Returns this >= 1 */
  def ? = Ge(this, ONE)
  /** Returns this <= 0 */
  def ! = Le(this, ZERO)

  /** Returns the value of the term */
  def value(solution: Solution): Int
}
/**
 * Object of `NIL` term.
 */
object NIL extends Term {
  def value(solution: Solution): Int =
    throw new IllegalArgumentException("NIL: value of NIL is not defined")
  override def toString = "nil"
}
/**
 * Case class of number terms.
 * @param value the value of the number term
 */
case class Num(value: Int) extends Term {
  def value(solution: Solution): Int = value
  override def toString = value.toString
}
/**
 * Object of number term 0.
 */
object ZERO extends Num(0)
/**
 * Object of number term 1.
 */
object ONE extends Num(1)
/**
 * Case class of integer variables.
 * @param name the primary name of the variable
 * @param is the indices of the variable (optional)
 */
case class Var(name: String, is: String*) extends Term with Ordering[Var] {
  private val str = name + " " + is.mkString(" ")
  /** Returns true when the variable auxiliary */
  var aux = false
  /** Returns a new variable with extra indices given by `is1` */
  def apply(is1: Any*) = {
    require(is1.forall{ case _: Expr => false case _ => true }, "Var: Expr cannot be used as an index")
    val v = Var(name, is ++ is1.map(_.toString): _*)
    v.aux = aux
    v
  }
  /** Compares variables */
  def compare(x1: Var, x2: Var) = {
    if (x1.is.size != x2.is.size)
      x1.is.size.compare(x2.is.size)
    else
      x1.str.compare(x2.str)
  }
  override def variables = Iterator(this)
  def value(solution: Solution): Int = solution.intValues(this)
  override def toString =
    if (is.size == 0) name else is.mkString(name + "(", ",", ")")
}
object Var {
  private var count = 0
  /** Returns a new anonymous variable */
  def apply() = {
    count += 1
    val v = new Var("_I" + count)
    v.aux = true
    v
  }
}
/**
 * Case class for absolute value of term.
 */
case class Abs(x0: Term) extends Term {
  override def variables = x0.variables
  override def bools = x0.bools
  def value(solution: Solution): Int = math.abs(x0.value(solution))
}
/**
 * Case class for negation of term.
 */
case class Neg(x0: Term) extends Term {
  override def variables = x0.variables
  override def bools = x0.bools
  def value(solution: Solution): Int = - x0.value(solution)
}
/**
 * Case class for addition of terms.
 * Companion object provies other factory methods.
 */
case class Add(xs: Term*) extends Term {
  override def variables = xs.toIterator.flatMap(_.variables)
  override def bools = xs.toIterator.flatMap(_.bools)
  def value(solution: Solution): Int = xs.map(_.value(solution)).sum
  override def toString = xs.mkString(productPrefix + "(", ",", ")")
}
/**
 * Factory for addition of terms.
 */
object Add {
  def apply(xs: Iterable[Term]) = new Add(xs.toSeq: _*)
}
/**
 * Factory for addition of terms.
 */
object Sum {
  def apply(xs: Iterable[Term]) = new Add(xs.toSeq: _*)
}
/**
 * Case class for subtraction of terms.
 * Companion object provies other factory methods.
 */
case class Sub(xs: Term*) extends Term {
  override def variables = xs.toIterator.flatMap(_.variables)
  override def bools = xs.toIterator.flatMap(_.bools)
  def value(solution: Solution): Int = xs.map(_.value(solution)) match {
    case Seq() => 0
    case Seq(a) => a
    case Seq(a, as @ _*) => a - as.sum
  }
  override def toString = xs.mkString(productPrefix + "(", ",", ")")
}
/**
 * Factory for subtraction of terms.
 */
object Sub {
  def apply(xs: Iterable[Term]) = new Sub(xs.toSeq: _*)
}
/**
 * Case class for multiplication of terms.
 * Companion object provies other factory methods.
 */
case class Mul(xs: Term*) extends Term {
  override def variables = xs.toIterator.flatMap(_.variables)
  override def bools = xs.toIterator.flatMap(_.bools)
  def value(solution: Solution): Int = xs.map(_.value(solution)).product
  override def toString = xs.mkString(productPrefix + "(", ",", ")")
}
/**
 * Factory for multiplication of terms.
 */
object Mul {
  def apply(xs: Iterable[Term]) = new Mul(xs.toSeq: _*)
}
/**
 * Case class for division of terms.
 */
case class Div(x0: Term, x1: Term) extends Term {
  override def variables = x0.variables ++ x1.variables
  override def bools = x0.bools ++ x1.bools
  def value(solution: Solution): Int = {
    val a0 = x0.value(solution)
    val a1 = x1.value(solution)
    if (a1 > 0) {
      if (a0 >= 0) a0/a1 else (a0-a1+1)/a1
    } else {
      if (a0 >= 0) (a0-a1-1)/a1 else a0/a1
    }
  }
}
/**
 * Case class for remainder of terms.
 */
case class Mod(x0: Term, x1: Term) extends Term {
  override def variables = x0.variables ++ x1.variables
  override def bools = x0.bools ++ x1.bools
  def value(solution: Solution): Int = {
    val r = x0.value(solution) % x1.value(solution)
    if (r > 0) r else r + x1.value(solution)
  }
}
/**
 * Case class for maximum of terms.
 * Companion object provies other factory methods.
 */
case class Max(xs: Term*) extends Term {
  override def variables = xs.toIterator.flatMap(_.variables)
  override def bools = xs.toIterator.flatMap(_.bools)
  def value(solution: Solution): Int = xs.map(_.value(solution)).max
  override def toString = xs.mkString(productPrefix + "(", ",", ")")
}
/**
 * Factory for maximum of terms.
 */
object Max {
  def apply(xs: Iterable[Term]) = new Max(xs.toSeq: _*)
}
/**
 * Case class for minimum of terms.
 * Companion object provies other factory methods.
 */
case class Min(xs: Term*) extends Term {
  override def variables = xs.toIterator.flatMap(_.variables)
  override def bools = xs.toIterator.flatMap(_.bools)
  def value(solution: Solution): Int = xs.map(_.value(solution)).min
  override def toString = xs.mkString(productPrefix + "(", ",", ")")
}
/**
 * Factory for minimum of terms.
 */
object Min {
  def apply(xs: Iterable[Term]) = new Min(xs.toSeq: _*)
}
/**
 * Case class for if expressions.
 */
case class If(c: Constraint, x0: Term, x1: Term) extends Term {
  override def variables = c.variables ++ x0.variables ++ x1.variables
  override def bools = c.bools ++ x0.bools ++ x1.bools
  def value(solution: Solution): Int =
    if (c.value(solution)) x0.value(solution) else x1.value(solution)
}

/**
 * Abstract class of constraints.
 *
 * Operators defined in this class create a new expression.
 * For example, `c && d` returns a new term `And(c, d)`
 * when `c` and `d` are constraints.
 */
sealed abstract class Constraint extends Expr {
  /** Returns [[copris.Not]] of Constraint */
  def unary_! = Not(this)
  /** Returns [[copris.And]] of Constraints */
  def && (c: Constraint) = And(this, c)
  /** Returns [[copris.Or]] of Constraints */
  def || (c: Constraint) = Or(this, c)
  /** Returns [[copris.Imp]] of Constraints */
  def ==> (c: Constraint) = Imp(this, c)
  /** Returns [[copris.Xor]] of Constraints */
  def ^ (c: Constraint) = Xor(this, c)
  /** Returns [[copris.Iff]] of Constraints */
  def <==> (c: Constraint) = Iff(this, c)
  /** Returns If(this, 1, 0) */
  def toInt = If(this, ONE, ZERO)
  /** Returns If(this, 1, 0) */
  def $ = toInt

  /** Returns the value of the constraint */
  def value(solution: Solution): Boolean
}
/**
 * Abstract class of global constraints.
 */
sealed abstract class GlobalConstraint extends Constraint

/**
 * Object of `FALSE` constraint.
 */
object FALSE extends Constraint {
  def value(solution: Solution): Boolean = false
  override def toString = "false"
}
/**
 * Object of `TRUE` constraint.
 */
object TRUE extends Constraint {
  def value(solution: Solution): Boolean = true
  override def toString = "true"
}
/**
 * Case class of Boolean variables.
 * @param name the primary name of the variable
 * @param is the indices of the variable (optional)
 */
case class Bool(name: String, is: String*) extends Constraint with Ordering[Bool] {
  private val str = name + " " + is.mkString(" ")
  /** Returns true when the variable auxiliary */
  var aux = false
  /** Returns a new variable with extra indices given by `is1` */
  def apply(is1: Any*) = {
    require(is1.forall{ case _: Expr => false case _ => true }, "Bool: Expr cannot be used as an index")
    val p = Bool(name, is ++ is1.map(_.toString): _*)
    p.aux = aux
    p
  }
  /** Compares variables */
  def compare(x1: Bool, x2: Bool) = {
    if (x1.is.size != x2.is.size)
      x1.is.size.compare(x2.is.size)
    else
      x1.str.compare(x2.str)
  }
  override def bools = Iterator(this)
  def value(solution: Solution): Boolean = solution.boolValues(this)
  override def toString =
    if (is.size == 0) name else is.mkString(name + "(", ",", ")")
}
object Bool {
  private var count = 0
  /** Returns a new anonymous variable */
  def apply() = {
    count += 1
    val p = new Bool("_B" + count)
    p.aux = true
    p
  }
}
/**
 * Case class for logical negation of constaint.
 */
case class Not(c0: Constraint) extends Constraint {
  override def variables = c0.variables
  override def bools = c0.bools
  def value(solution: Solution): Boolean = ! c0.value(solution)
}
/**
 * Case class for conjuction of constaints.
 * Companion object provies other factory methods.
 */
case class And(cs: Constraint*) extends Constraint {
  override def variables = cs.toIterator.flatMap(_.variables)
  override def bools = cs.toIterator.flatMap(_.bools)
  def value(solution: Solution): Boolean = cs.forall(_.value(solution))
  override def toString = cs.mkString(productPrefix + "(", ",", ")")
}
/**
 * Factory for conjunction of terms.
 */
object And {
  def apply(xs: Iterable[Constraint]) = new And(xs.toSeq: _*)
}
/**
 * Case class for disjuction of constaints.
 * Companion object provies other factory methods.
 */
case class Or(cs: Constraint*) extends Constraint {
  override def variables = cs.toIterator.flatMap(_.variables)
  override def bools = cs.toIterator.flatMap(_.bools)
  def value(solution: Solution): Boolean = cs.exists(_.value(solution))
  override def toString = cs.mkString(productPrefix + "(", ",", ")")
}
/**
 * Factory for disjunction of terms.
 */
object Or {
  def apply(xs: Iterable[Constraint]) = new Or(xs.toSeq: _*)
}
/**
 * Case class for implication of constaint.
 */
case class Imp(c0: Constraint, c1: Constraint) extends Constraint {
  override def variables = c0.variables ++ c1.variables
  override def bools = c0.bools ++ c1.bools
  def value(solution: Solution): Boolean = ! c0.value(solution) || c1.value(solution)
}
/**
 * Case class for exclusive-or of constaint.
 */
case class Xor(c0: Constraint, c1: Constraint) extends Constraint {
  override def variables = c0.variables ++ c1.variables
  override def bools = c0.bools ++ c1.bools
  def value(solution: Solution): Boolean = c0.value(solution) ^ c1.value(solution)
}
/**
 * Case class for if-and-only-if of constaint.
 */
case class Iff(c0: Constraint, c1: Constraint) extends Constraint {
  override def variables = c0.variables ++ c1.variables
  override def bools = c0.bools ++ c1.bools
  def value(solution: Solution): Boolean = c0.value(solution) == c1.value(solution)
}
/**
 * Case class for equals constraints.
 */
case class Eq(x0: Term, x1: Term) extends Constraint {
  override def variables = x0.variables ++ x1.variables
  override def bools = x0.bools ++ x1.bools
  def value(solution: Solution): Boolean = x0.value(solution) == x1.value(solution)
}
/**
 * Case class for not-equals constraints.
 */
case class Ne(x0: Term, x1: Term) extends Constraint {
  override def variables = x0.variables ++ x1.variables
  override def bools = x0.bools ++ x1.bools
  def value(solution: Solution): Boolean = x0.value(solution) != x1.value(solution)
}
/**
 * Case class for less-than-or-equals constraints.
 */
case class Le(x0: Term, x1: Term) extends Constraint {
  override def variables = x0.variables ++ x1.variables
  override def bools = x0.bools ++ x1.bools
  def value(solution: Solution): Boolean = x0.value(solution) <= x1.value(solution)
}
/**
 * Case class for less-than constraints.
 */
case class Lt(x0: Term, x1: Term) extends Constraint {
  override def variables = x0.variables ++ x1.variables
  override def bools = x0.bools ++ x1.bools
  def value(solution: Solution): Boolean = x0.value(solution) < x1.value(solution)
}
/**
 * Case class for greater-than-or-equals constraints.
 */
case class Ge(x0: Term, x1: Term) extends Constraint {
  override def variables = x0.variables ++ x1.variables
  override def bools = x0.bools ++ x1.bools
  def value(solution: Solution): Boolean = x0.value(solution) >= x1.value(solution)
}
/**
 * Case class for greater-than constraints.
 */
case class Gt(x0: Term, x1: Term) extends Constraint {
  override def variables = x0.variables ++ x1.variables
  override def bools = x0.bools ++ x1.bools
  def value(solution: Solution): Boolean = x0.value(solution) > x1.value(solution)
}
/**
 * Case class for Alldifferent global constraints.
 * Companion object provies other factory methods.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Calldifferent.html]]
 */
case class Alldifferent(xs: Term*) extends GlobalConstraint {
  override def variables = xs.toIterator.flatMap(_.variables)
  override def bools = xs.toIterator.flatMap(_.bools)
  def value(solution: Solution): Boolean = {
    val as = xs.map(_.value(solution))
    (0 until as.size).forall(i => (i+1 until as.size).forall(j => as(i) != as(j)))
  }
  override def toString = xs.mkString(productPrefix + "(", ",", ")")
}
/**
 * Factory for Alldifferent global constraints.
 */
object Alldifferent {
  def apply(xs: Iterable[Term]) = new Alldifferent(xs.toSeq: _*)
}
/**
 * Case class for Weightedsum global constraints.
 */
case class Weightedsum(axs: Seq[(Int,Term)], cmp: String, b: Term) extends GlobalConstraint {
  require(cmp.matches("eq|ne|lt|le|gt|ge"), "Weightedsum: Comparison should be one of eq,ne,lt,le,gt,ge")
  override def variables = axs.toIterator.flatMap(_._2.variables) ++ b.variables
  override def bools = axs.toIterator.flatMap(_._2.bools) ++ b.bools
  def value(solution: Solution): Boolean = {
    val sum = axs.map(ax => ax._1 * ax._2.value(solution)).sum
    cmp match {
      case "eq" => sum == b.value(solution)
      case "ne" => sum != b.value(solution)
      case "lt" => sum <  b.value(solution)
      case "le" => sum <= b.value(solution)
      case "gt" => sum >  b.value(solution)
      case "ge" => sum >= b.value(solution)
    }
  }
  override def toString =
    productPrefix + "(" + axs + "," + cmp + "," + b + ")"
}
/**
 * Case class for Cumulative global constraints.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Ccumulative.html]]
 */
case class Cumulative(tasks: Seq[(Term,Term,Term,Term)], limit: Term) extends GlobalConstraint {
  override def variables = {
    val vs = for (p <- tasks.toIterator; t <- p.productIterator; v <- t.asInstanceOf[Expr].variables) yield v
    vs ++ limit.variables
  }
  override def bools = {
    val vs = for (p <- tasks.toIterator; t <- p.productIterator; v <- t.asInstanceOf[Expr].bools) yield v
    vs ++ limit.bools
  }
  def value(solution: Solution): Boolean = {
    def value(x: Term) = x.value(solution)
    def taskBlock(task: (Term,Term,Term,Term)) = {
      val (origin, duration, end, height) = task
      if (origin == NIL)
	(value(end) - value(duration), value(end), value(height))
      else if (end == NIL)
	(value(origin), value(origin) + value(duration), value(height))
      else
	(value(origin), value(end), value(height))
    }
    val taskBlocks = tasks.map(taskBlock(_))
    val timeBlocks = (taskBlocks.map(_._1).toSet | taskBlocks.map(_._2).toSet).toSeq.sorted.sliding(2)
    val lim = value(limit)
    timeBlocks.forall {case Seq(t0,t1) => {
      val hs = for ((o,e,h) <- taskBlocks)
	       yield if (t0 <= o && e <= t1) h else 0
      hs.sum <= lim
    }}
  }
  override def toString =
    productPrefix + "(" + tasks + "," + limit + ")"
}
/**
 * Case class for Element global constraints.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Celement.html]]
 */
case class Element(i: Term, xs: Seq[Term], xi: Term) extends GlobalConstraint {
  override def variables = i.variables ++ xs.toIterator.flatMap(_.variables) ++ xi.variables
  override def bools = i.bools ++ xs.toIterator.flatMap(_.bools) ++ xi.bools
  def value(solution: Solution): Boolean = {
    val as = xs.map(_.value(solution))
    as(i.value(solution) - 1) == xi.value(solution)
  }
  override def toString =
    productPrefix + "(" + i + "," + xs + "," + xi + ")"
}
/**
 * Case class for Disjunctive global constraints.
 * Companion object provies other factory methods.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Cdisjunctive.html]]
 */
case class Disjunctive(tasks: (Term,Term)*) extends GlobalConstraint {
  override def variables =
    for (p <- tasks.toIterator; t <- p.productIterator; v <- t.asInstanceOf[Expr].variables) yield v
  override def bools =
    for (p <- tasks.toIterator; t <- p.productIterator; v <- t.asInstanceOf[Expr].bools) yield v
  def value(solution: Solution): Boolean = {
    val ts = tasks.map(task => (task._1.value(solution), task._2.value(solution)))
    (0 until ts.size).forall { i =>
      (i+1 until ts.size).forall { j =>
	ts(i)._1 + ts(i)._2 <= ts(j)._1 || ts(j)._1 + ts(j)._2 <= ts(i)._1
      }
    }
  }
  override def toString =
    productPrefix + "(" + tasks + ")"
}
/**
 * Factory for Disjunctive global constraints.
 */
object Disjunctive {
  def apply(tasks: Iterable[(Term,Term)]) = new Disjunctive(tasks.toSeq: _*)
}
/**
 * Case class for LexLess global constraints.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Clex_less.html]]
 */
case class LexLess(xs: Seq[Term], ys: Seq[Term]) extends GlobalConstraint {
  override def variables = xs.toIterator.flatMap(_.variables) ++ ys.toIterator.flatMap(_.variables)
  override def bools = xs.toIterator.flatMap(_.bools) ++ ys.toIterator.flatMap(_.bools)
  def value(solution: Solution): Boolean = {
    def less(as: Seq[Int], bs: Seq[Int]): Boolean =
      if (as.isEmpty || bs.isEmpty)
	as.isEmpty && ! bs.isEmpty
      else
	as.head < bs.head || (as.head == bs.head && less(as.tail, bs.tail))
    less(xs.map(_.value(solution)), ys.map(_.value(solution)))
  }
  override def toString =
    productPrefix + "(" + xs + "," + ys + ")"
}
/**
 * Case class for LexLesseq global constraints.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Clex_lesseq.html]]
 */
case class LexLesseq(xs: Seq[Term], ys: Seq[Term]) extends GlobalConstraint {
  override def variables = xs.toIterator.flatMap(_.variables) ++ ys.toIterator.flatMap(_.variables)
  override def bools = xs.toIterator.flatMap(_.bools) ++ ys.toIterator.flatMap(_.bools)
  def value(solution: Solution): Boolean = {
    def lessEq(as: Seq[Int], bs: Seq[Int]): Boolean =
      if (as.isEmpty || bs.isEmpty)
	as.isEmpty
      else
	as.head < bs.head || (as.head == bs.head && lessEq(as.tail, bs.tail))
    lessEq(xs.map(_.value(solution)), ys.map(_.value(solution)))
  }
  override def toString =
    productPrefix + "(" + xs + "," + ys + ")"
}
/**
 * Case class for Nvalue global constraints.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Cnvalue.html]]
 */
case class Nvalue(count: Term, xs: Seq[Term]) extends GlobalConstraint {
  override def variables = count.variables ++ xs.toIterator.flatMap(_.variables)
  override def bools = count.bools ++ xs.toIterator.flatMap(_.bools)
  def value(solution: Solution): Boolean =
    xs.map(_.value(solution)).toSet.size == count.value(solution)
  override def toString =
    productPrefix + "(" + count + "," + xs + ")"
}
/**
 * Case class for GlobalCardinality global constraints.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Cglobal_cardinality.html]]
 */
case class GlobalCardinality(xs: Seq[Term], card: Seq[(Int,Term)]) extends GlobalConstraint {
  override def variables = xs.toIterator.flatMap(_.variables) ++ card.toIterator.flatMap(_._2.variables)
  override def bools = xs.toIterator.flatMap(_.bools) ++ card.toIterator.flatMap(_._2.bools)
  def value(solution: Solution): Boolean = {
    val as = xs.map(_.value(solution))
    card.forall {
      case (b, c) => as.count(_ == b) == c.value(solution)
    }
  }
  override def toString =
    productPrefix + "(" + xs + "," + card + ")"
}
/**
 * Case class for GlobalCardinalityWithCosts global constraints.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Cglobal_cardinality_with_costs.html]]
 */
case class GlobalCardinalityWithCosts(xs: Seq[Term], card: Seq[(Int,Term)],
                                      table: Seq[(Int,Int,Int)], cost: Term) extends GlobalConstraint {
  override def variables = xs.toIterator.flatMap(_.variables) ++ card.toIterator.flatMap(_._2.variables) ++ cost.variables
  override def bools = xs.toIterator.flatMap(_.bools) ++ card.toIterator.flatMap(_._2.bools) ++ cost.bools
  def value(solution: Solution): Boolean = {
    def getCost(i: Int, a: Int) = {
      val j = card.indexWhere(_._1 == a) + 1
      table.find(t => t._1 == i && t._2 == j).get._3
    }
    val as = xs.map(_.value(solution))
    card.forall {
      case (b, c) => as.count(_ == b) == c.value(solution)
    } && {
      val cs = for (i <- 1 to as.size) yield getCost(i, as(i - 1))
      cs.sum == cost.value(solution)
    }
  }
  override def toString =
    productPrefix + "(" + xs + "," + card  + "," + table  + "," + cost + ")"
}
/**
 * Case class for Count global constraints.
 * @see [[http://www.emn.fr/z-info/sdemasse/gccat/Ccount.html]]
 */
case class Count(value: Term, xs: Seq[Term], cmp: String, count: Term) extends GlobalConstraint {
  require(cmp.matches("eq|ne|lt|le|gt|ge"), "Count: Comparison should be one of eq,ne,lt,le,gt,ge")
  override def variables = value.variables ++ xs.toIterator.flatMap(_.variables) ++ count.variables
  override def bools = value.bools ++ xs.toIterator.flatMap(_.bools) ++ count.bools
  def value(solution: Solution): Boolean = {
    val a = value.value(solution)
    val c = xs.map(_.value(solution)).count(_ == a)
    cmp match {
      case "eq" => c == count.value(solution)
      case "ne" => c != count.value(solution)
      case "lt" => c <  count.value(solution)
      case "le" => c <= count.value(solution)
      case "gt" => c >  count.value(solution)
      case "ge" => c >= count.value(solution)
    }
  }
  override def toString =
    productPrefix + "(" + value + "," + xs + "," + cmp + "," + count + ")"
}

/**
 * Abstract class of domains.
 * Companion object provies other factory methods.
 */
abstract class Domain {
  /** Returns lower bound value */
  def lb: Int
  /** Returns upper bound value */
  def ub: Int
  /** Checks the domain contains the given value */
  def contains(a: Int): Boolean
}
/**
 * Case class of interval domain
 */
case class IntervalDomain(lo: Int, hi: Int) extends Domain {
  require(lo <= hi, s"IntervalDomain: $lo > $hi")
  def lb = lo
  def ub = hi
  def contains(a: Int): Boolean = lo <= a && a <= hi
  override def productPrefix = "Domain"
}
/**
 * Case class of set domain
 */
case class SetDomain(values: SortedSet[Int]) extends Domain {
  def lb = values.min
  def ub = values.max
  def contains(a: Int): Boolean = values.contains(a)
  override def productPrefix = "Domain"
}
/**
 * Factory for creating domains.
 */
object Domain {
  /** Returns [[copris.IntervalDomain]] */
  def apply(lo: Int, hi: Int) =
    IntervalDomain(lo, hi)
  /** Returns [[copris.IntervalDomain]] with singleton value */
  def apply(value: Int) =
    IntervalDomain(value, value)
  /** Returns [[copris.SetDomain]] */
  def apply(values: Set[Int]) =
    SetDomain(SortedSet(values.toSeq: _*))
}
/**
 * Case class of enum domain
 */
case class EnumDomain[A](values: A*) {
  /** Returns the size of the EnumDomain */
  def size = values.size
  /** Returns the i-th value */
  def apply(i: Int): A = values(i)
  /** Checks the domain contains the given value */
  def contains(value: A) = values.contains(value)
  /** Returns the index of the given value */
  def id(value: A): Int = values.indexOf(value)
  override def toString =
    productPrefix + values.mkString("(", ",", ")")
}

/**
 * Trait of CSP (Constraint Satisfaction Problem)
 */
trait CSPTrait {
  /** Adds an integer variable */
  def int(x: Var, d: Domain): Var
  /** Adds an integer variable */
  def int(x: Var, d: Set[Int]): Var =
    int(x, Domain(d))
  /** Adds an integer variable */
  def int(x: Var, lo: Int, hi: Int): Var =
    int(x, Domain(lo, hi))
  /** Adds an integer variable */
  def int(x: Var, value: Int): Var =
    int(x, Domain(value))
  /** Adds integer variables */
  def int(xs: Iterable[Term], d: Domain): Iterable[Term] = {
    xs.foreach(_ match {
      case x: Var => int(x, d)
      case _ =>
        throw new IllegalArgumentException("int: argument of int declaration should be a Var")
    })
    xs
  }
  /** Adds integer variables */
  def int(xs: Iterable[Term], d: Set[Int]): Iterable[Term] =
    int(xs, Domain(d))
  /** Adds integer variables */
  def int(xs: Iterable[Term], lo: Int, hi: Int): Iterable[Term] =
    int(xs, Domain(lo, hi))
  /** Adds integer variables */
  def int(xs: Iterable[Term], value: Int): Iterable[Term] =
    int(xs, Domain(value))

  /** Adds a 0-1 integer variable */
  def boolInt(x: Var): Var
  /** Adds 0-1 integer variables */
  def boolInt(xs: Iterable[Term]): Iterable[Term] = {
    xs.foreach(_ match {
      case x: Var => boolInt(x)
      case _ =>
        throw new IllegalArgumentException("boolInt: argument of boolInt declaration should be a Var")
    })
    xs
  }

  /** Adds a Boolean variable */
  def bool(p: Bool): Bool
  /** Adds Boolean variables */
  def bool(ps: Iterable[Bool]): Iterable[Bool] =
    { ps.foreach(bool(_)); ps }

  /** Adds a constraint */
  def add(cs: Constraint*): Unit
  /** Adds constraints */
  def add(cs: Iterable[Constraint]): Unit =
    add(cs.toSeq: _*)

  /** Specifies objective variable to be minimized */
  def minimize(x: Var): Var
  /** Specifies objective variable to be maximized */
  def maximize(x: Var): Var

  /** Checks whether the CSP is satisfied by the solution */
  def satisfiedBy(solution: Solution): Boolean
}

/**
 * Case class of CSP (Constraint Satisfaction Problem)
 * @param variables integer variables
 * @param bools Boolean variables
 * @param dom domains of integer variables
 * @param constraints constraints
case class CSP(var variables: Seq[Var] = Seq.empty,
               var bools: Seq[Bool] = Seq.empty,
               var dom: Map[Var,Domain] = Map.empty,
               var constraints: Seq[Constraint] = Seq.empty)
 */
case class CSP(var variables: IndexedSeq[Var] = IndexedSeq(),
               var bools: IndexedSeq[Bool] = IndexedSeq(),
               var dom: Map[Var,Domain] = Map(),
               var constraints: IndexedSeq[Constraint] = IndexedSeq())
  extends CSPTrait {
  private var variablesSet: Set[Var] = Set.empty
  private var variablesSize = 0
  private var boolsSet: Set[Bool] = Set.empty
  private var boolsSize = 0
  private var constraintsSize = 0
  /** Objective variable.  `null` if not defined */
  var objective: Var = null
  private var target = 0

  /**
   * Creates a copy of the given CSP
   * @param csp0 original CSP
   */
  def this(csp0: CSP) = {
    this(csp0.variables, csp0.bools, csp0.dom, csp0.constraints)
    objective = csp0.objective
    target = csp0.target
    commit
  }
  /**
   * Resets the CSP by setting variables, bools, dom, and
   * constraints to be empty.
   */
  def init: Unit = {
    variables = IndexedSeq.empty; bools = IndexedSeq.empty
    dom = Map.empty; constraints = IndexedSeq.empty
    variablesSet = Set.empty; variablesSize = 0
    boolsSet = Set.empty; boolsSize = 0
    constraintsSize = 0
    objective = null; target = 0
  }
  /**
   * Adds an integer variable
   */
  def int(x: Var, d: Domain): Var = {
    if (variablesSet.contains(x))
      throw new IllegalArgumentException("int: duplicate int declaration of " + x)
    variablesSet += x; variables = variables :+ x; dom += x -> d; x
  }
  /**
   * Adds a 0-1 integer variable
   */
  def boolInt(x: Var): Var =
    int(x, 0, 1)
  /**
   * Adds a Boolean variable
   */
  def bool(p: Bool): Bool = {
    if (boolsSet.contains(p))
      throw new IllegalArgumentException("bool: duplicate bool declaration of " + p)
    boolsSet += p; bools = bools :+ p; p
  }
  /**
   * Adds constraints
   */
  def add(cs: Constraint*): Unit = {
    val badVariables = cs.toIterator.flatMap(_.variables).filter(! variablesSet.contains(_))
    val badBools = cs.toIterator.flatMap(_.bools).filter(! boolsSet.contains(_))
    if (! badVariables.isEmpty)
      throw new IllegalArgumentException("add: undeclared int variables " + badVariables.mkString(","))
    if (! badBools.isEmpty)
      throw new IllegalArgumentException("add: undeclared bool variables " + badBools.mkString(","))
    if (cs.size == 1)
      constraints = constraints :+ cs(0)
    else
      constraints = constraints ++ cs
  }
  /**
   * Commits the changes made for the CSP.
   */
  def commit: Unit = {
    variablesSize = variables.size
    boolsSize = bools.size
    constraintsSize = constraints.size
  }
  /**
   * Cancels the changes made for the CSP.
   */
  def cancel: Unit = {
    variables = variables.take(variablesSize)
    variablesSet = variables.toSet
    bools = bools.take(boolsSize)
    boolsSet = bools.toSet
    constraints = constraints.take(constraintsSize)
  }
  /**
   * Returns the integer variables added after the last commit.
   */
  def variablesDelta =
    variables.drop(variablesSize)
  /**
   * Returns the Boolean variables added after the last commit.
   */
  def boolsDelta =
    bools.drop(boolsSize)
  /**
   * Returns the constraints added after the last commit.
   */
  def constraintsDelta =
    constraints.drop(constraintsSize)
  /**
   * Specifies the objective variable to be minimized
   */
  def minimize(x: Var): Var = {
    objective = x
    target = -1
    x
  }
  /**
   * Specifies the objective variable to be maximized
   */
  def maximize(x: Var): Var = {
    objective = x
    target = 1
    x
  }
  /**
   * Returns true when the minimization is specified
   */
  def isMinimize = target < 0
  /**
   * Returns true when the maximization is specified
   */
  def isMaximize = target > 0
  /* */
  def satisfiedBy(solution: Solution): Boolean = {
    variables.forall {
      x => dom(x).contains(x.value(solution))
    } && constraints.forall {
      c => c.value(solution)
    }
  }
  /**
   * Returns the readable String representation of the CSP
   */
  def output: String = {
    val sb = new StringBuilder()
    for (x <- variables) dom(x) match {
      case d: IntervalDomain =>
        sb.append("int(" + x + "," + d.lo + "," + d.hi + ")\n")
      case d: SetDomain =>
        sb.append("int(" + x + "," + d + ")\n")
    }
    for (p <- bools)
      sb.append("bool(" + p + ")\n")
    for (c <- constraints)
      sb.append(c.toString + "\n")
    if (isMinimize)
      sb.append("minimize(" + objective + ")\n")
    else if (isMaximize)
      sb.append("maximize(" + objective + ")\n")
    sb.toString
  }
}

/*
 * Statement (this trait is not used)
 * Constraint class is also a Statement
 */
/*
trait Statement
case class IntStatement(x: Var, d: Domain) extends Statement
case class BoolStatement(p: Bool) extends Statement
case class ConstraintStatement(c: Constraint) extends Statement
case class MinimizeStatement(x: Var) extends Statement
case class MaximizeStatement(x: Var) extends Statement
*/
