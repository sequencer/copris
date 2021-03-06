package copris

/**
 * Trait for Copris DSL which provides methods for CSP and CSP solver.
 * This trait also provides implicit conversion of converting
 * scala Symbols to CSP integer variables ([[copris.Var]]).
 */
trait CoprisTrait extends CSPTrait with SolverTrait {
  import scala.language.implicitConversions
  /** Implicit conversion from scala Symbol to [[copris.Var]]. */
  implicit def symbol2var(s: Symbol) = Var(s.name)
  // /** Implicit conversion from scala Symbol to [[copris.Constraint]]. */
  // implicit def symbol2constraint(s: Symbol) = Ne(Var(s.name), ZERO)
  /** CSP to be used */
  def csp: CSP
  /** Solver to be used */
  def solver: AbstractSolver
  /** Changes the solver to be used */
  def use(newSolver: AbstractSolver): Unit
  /** Gets the options of the solver */
  def options = solver.options
  /** Sets the options of the solver */
  def setOptions(opts: Map[String,String]): Unit =
    solver.options = opts
  /** Initializes the CSP and solver */
  def init { csp.init; solver.init }
  /* */
  def commit { csp.commit; solver.commit }
  /* */
  def cancel { csp.cancel; solver.cancel }
  /* */
  def int(x: Var, d: Domain) = csp.int(x, d)
  /* */
  def int[A](x: Var, enum: EnumDomain[A]) = csp.int(x, 0, enum.size - 1)
  /* */
  def boolInt(x: Var) = csp.boolInt(x)
  /* */
  def bool(p: Bool) = csp.bool(p)
  /* */
  def add(cs: Constraint*) = csp.add(cs: _*)
  /* */
  def minimize(x: Var) = csp.minimize(x)
  /* */
  def maximize(x: Var) = csp.maximize(x)
  /* */
  def satisfiedBy(solution: Solution) = csp.satisfiedBy(solution)
  /* */
  def find = solver.find
  /* */
  def findNext = solver.findNext
  /* */
  def findOpt = solver.findOpt
  /** Shows the CSP */
  def show = print(csp.output)
  /* */
  def dump(fileName: String) { solver.dump(fileName, "") }
  /* */
  def dump(fileName: String, format: String) { solver.dump(fileName, format) }
  /** Returns the current solution */
  def solution = solver.solution
  /** Returns the iterator of all solutions */
  def solutions = solver.solutions
  /** Starts the timer (experimental) */
  def startTimer(timeout: Long) = solver.startTimer(timeout)
  /** Stops the timer (experimental) */
  def stopTimer = solver.stopTimer
  /** Returns the info of the solver (experimental) */
  def info = solver.solverInfo
  /** Returns the status of the solver (experimental) */
  def stats = solver.solverStats
}

/**
 * Class for Copris DSL which provides methods for CSP and CSP solver.
 * @constructor Constructs Copris with the given CSP and solver
 */
class Copris(val csp: CSP, var solver: AbstractSolver) extends CoprisTrait {
  /** Constructs Copris with the given CSP and [[copris.DefaultSolver]] */
  def this(csp: CSP) =
    this(csp, DefaultSolver(csp))
  /** Constructs Copris with empty CSP and [[copris.DefaultSolver]] */
  def this() =
    this(CSP())
  /** Changes the solver to be used */
  def use(newSolver: AbstractSolver): Unit =
    solver = newSolver
}

/**
 * Object for Copris DSL which provides methods for CSP and CSP solver.
 */
object dsl extends CoprisTrait {
  /** Dynamic variable of Copris */
  val coprisVar = new util.DynamicVariable[CoprisTrait](new Copris())
  /** Returns Copris object */
  def copris = coprisVar.value
  /** Returns CSP */
  def csp = coprisVar.value.csp
  /** Returns CSP solver */
  def solver = coprisVar.value.solver
  /* */
  def use(newSolver: AbstractSolver) =
    coprisVar.value.use(newSolver)
  /** Executes the `block` under the specified Copris */
  def using(copris: CoprisTrait = new Copris())(block: => Unit) =
    coprisVar.withValue(copris) { block }
}
