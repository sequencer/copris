package copris
import sugar.expression.{Expression => SugarExpr}

/**
 * Class for translating CSP to a list of Sugar expressions
 */
class Translator {
  private var sugarVarNameMap: Map[Var,String] = Map.empty
  private var sugarBoolNameMap: Map[Bool,String] = Map.empty
  def createSugarExpr(x: SugarExpr, xs: SugarExpr*) =
    SugarExpr.create(x, xs.toArray)
  def toSugarName(name: String, is: String*): String =
    (Seq(name) ++ is.map(_.toString)).mkString("_").flatMap { c =>
      if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') ||
        ('0' <= c && c <= '9') || "_+-*/%=<>!&|".contains(c) ||
        (0x000080 <= c && c <= 0x10FFFF))
        c.toString
      else if (c < 0x100)
        "$%02x".format(c.toInt)
      else
        "$%04x".format(c.toInt)
    }
  def toSugarName(x: Var): String =
    sugarVarNameMap.getOrElse(x, {
      val s = toSugarName(x.name, x.is: _*)
      sugarVarNameMap += x -> s
      s
    })
  def toSugarName(p: Bool): String =
    sugarBoolNameMap.getOrElse(p, {
      val s = toSugarName(p.name, p.is: _*)
      sugarBoolNameMap += p -> s
      s
    })
  def toSugar(x: Term): SugarExpr = x match {
    case NIL =>
      SugarExpr.NIL
    case Num(value) =>
      SugarExpr.create(value)
    case Var(name, is @ _*) =>
      SugarExpr.create(toSugarName(name, is: _*))
    case Abs(x0) =>
      createSugarExpr(SugarExpr.ABS, toSugar(x0))
    case Neg(x0) =>
      createSugarExpr(SugarExpr.NEG, toSugar(x0))
    case Add(xs @ _*) =>
      createSugarExpr(SugarExpr.ADD, xs.map(toSugar(_)): _*)
    case Sub(xs @ _*) =>
      createSugarExpr(SugarExpr.SUB, xs.map(toSugar(_)): _*)
    case Mul(xs @ _*) =>
      xs.map(toSugar(_)).reduceLeft(createSugarExpr(SugarExpr.MUL, _, _))
    case Div(x0, x1) =>
      createSugarExpr(SugarExpr.DIV, toSugar(x0), toSugar(x1))
    case Mod(x0, x1) =>
      createSugarExpr(SugarExpr.MOD, toSugar(x0), toSugar(x1))
    case Max(xs @ _*) =>
      xs.map(toSugar(_)).reduceLeft(createSugarExpr(SugarExpr.MAX, _, _))
    case Min(xs @ _*) =>
      xs.map(toSugar(_)).reduceLeft(createSugarExpr(SugarExpr.MIN, _, _))
    case If(c, x0, x1) =>
      createSugarExpr(SugarExpr.IF, toSugar(c), toSugar(x0), toSugar(x1))
  }
  def toSugar(c: Constraint): SugarExpr = c match {
    case FALSE =>
      SugarExpr.FALSE
    case TRUE =>
      SugarExpr.TRUE
    case Bool(name, is @ _*) =>
      SugarExpr.create(toSugarName(name, is: _*))
    case Not(c0) =>
      createSugarExpr(SugarExpr.NOT, toSugar(c0))
    case And(cs @ _*) =>
      createSugarExpr(SugarExpr.AND, cs.map(toSugar(_)): _*)
    case Or(cs @ _*) =>
      createSugarExpr(SugarExpr.OR, cs.map(toSugar(_)): _*)
    case Imp(c0, c1) =>
      createSugarExpr(SugarExpr.IMP, toSugar(c0), toSugar(c1))
    case Xor(c0, c1) =>
      createSugarExpr(SugarExpr.XOR, toSugar(c0), toSugar(c1))
    case Iff(c0, c1) =>
      createSugarExpr(SugarExpr.IFF, toSugar(c0), toSugar(c1))
    case Eq(x0, x1) =>
      createSugarExpr(SugarExpr.EQ, toSugar(x0), toSugar(x1))
    case Ne(x0, x1) =>
      createSugarExpr(SugarExpr.NE, toSugar(x0), toSugar(x1))
    case Le(x0, x1) =>
      createSugarExpr(SugarExpr.LE, toSugar(x0), toSugar(x1))
    case Lt(x0, x1) =>
      createSugarExpr(SugarExpr.LT, toSugar(x0), toSugar(x1))
    case Ge(x0, x1) =>
      createSugarExpr(SugarExpr.GE, toSugar(x0), toSugar(x1))
    case Gt(x0, x1) =>
      createSugarExpr(SugarExpr.GT, toSugar(x0), toSugar(x1))
    case Alldifferent(xs @ _*) =>
      createSugarExpr(SugarExpr.ALLDIFFERENT, xs.map(toSugar(_)): _*)
    case Weightedsum(axs, cmp, b) =>
      createSugarExpr(SugarExpr.WEIGHTEDSUM, toSugarAny(axs), toSugarAny(cmp), toSugar(b))
    case Cumulative(tasks, limit) =>
      createSugarExpr(SugarExpr.CUMULATIVE, toSugarAny(tasks), toSugar(limit))
    case Element(i, xs, xi) =>
      createSugarExpr(SugarExpr.ELEMENT, toSugar(i), toSugarAny(xs), toSugar(xi))
    case Disjunctive(tasks @ _*) =>
      createSugarExpr(SugarExpr.DISJUNCTIVE, toSugarAny(tasks))
    case LexLess(xs, ys) =>
      createSugarExpr(SugarExpr.LEX_LESS, toSugarAny(xs), toSugarAny(ys))
    case LexLesseq(xs, ys) =>
      createSugarExpr(SugarExpr.LEX_LESSEQ, toSugarAny(xs), toSugarAny(ys))
    case Nvalue(count, xs) =>
      createSugarExpr(SugarExpr.NVALUE, toSugar(count), toSugarAny(xs))
    case GlobalCardinality(xs, card) =>
      createSugarExpr(SugarExpr.GLOBAL_CARDINALITY, toSugarAny(xs), toSugarAny(card))
    case GlobalCardinalityWithCosts(xs, card, table, cost) =>
      createSugarExpr(SugarExpr.GLOBAL_CARDINALITY_WITH_COSTS,
        toSugarAny(xs), toSugarAny(card), toSugarAny(table), toSugar(cost))
    case Count(value, xs, cmp, count) =>
      createSugarExpr(SugarExpr.COUNT,
        toSugar(value), toSugarAny(xs), toSugarAny(cmp), toSugar(count))
  }
  def toSugarAny(a: Any): SugarExpr = a match {
    case i: Int => SugarExpr.create(i)
    case s: String => SugarExpr.create(s)
    case x: Term => toSugar(x)
    case xs: Seq[Any] =>
      SugarExpr.create(xs.map(toSugarAny(_)).toArray)
    case p: (Any,Any) =>
      createSugarExpr(toSugarAny(p._1), toSugarAny(p._2))
    case p: (Any,Any,Any) =>
      createSugarExpr(toSugarAny(p._1), toSugarAny(p._2), toSugarAny(p._3))
    case p: (Any,Any,Any,Any) =>
      createSugarExpr(toSugarAny(p._1), toSugarAny(p._2), toSugarAny(p._3), toSugarAny(p._4))
  }
  def toSugarInt(csp: CSP, x: Var) = csp.dom(x) match {
    case d: IntervalDomain => {
      val lo = SugarExpr.create(d.lo)
      val hi = SugarExpr.create(d.hi)
      SugarExpr.create(SugarExpr.INT_DEFINITION, toSugar(x), lo, hi)
    }
    case d: SetDomain => {
      val dom = d.values.toList.sortWith(_ < _).map(SugarExpr.create(_))
      val xs = toSugar(x) :: SugarExpr.create(dom.toArray) :: Nil
      SugarExpr.create(SugarExpr.INT_DEFINITION, xs.toArray)
    }
  }
  def toSugarBool(csp: CSP, p: Bool) =
    SugarExpr.create(SugarExpr.BOOL_DEFINITION, toSugar(p))
  def toSugar(csp: CSP, outputObjective: Boolean = true): java.util.ArrayList[SugarExpr] = {
    val expressions = new java.util.ArrayList[SugarExpr]()
    // println("Translating integer variables")
    csp.variables.foreach(v => expressions.add(toSugarInt(csp, v)))
    // println("Translating boolean variables")
    csp.bools.foreach(p => expressions.add(toSugarBool(csp, p)))
    if (outputObjective && csp.objective != null) {
      // println("Translating objective variable")
      val x = createSugarExpr(
        SugarExpr.OBJECTIVE_DEFINITION,
        if (csp.isMinimize) SugarExpr.MINIMIZE else SugarExpr.MAXIMIZE,
        toSugar(csp.objective)
      )
      expressions.add(x)
    }
    // println("Translating constraints")
    // csp.constraints.foreach(c => expressions.add(toSugar(c)))
    val n = csp.constraints.size
    for (i <- 0 until n) {
      // if (i % 10000 == 0)
      // println("Translating " + i + "/" + n + " constraints")
      expressions.add(toSugar(csp.constraints(i)))
    }
    expressions
  }
  def toSugarDelta(csp: CSP): java.util.ArrayList[SugarExpr] = {
    val expressions = new java.util.ArrayList[SugarExpr]()
    csp.variablesDelta.foreach(v => expressions.add(toSugarInt(csp, v)))
    csp.boolsDelta.foreach(p => expressions.add(toSugarBool(csp, p)))
    csp.constraintsDelta.foreach(c => expressions.add(toSugar(c)))
    expressions
  }
}