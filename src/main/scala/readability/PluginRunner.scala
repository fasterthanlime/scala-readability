package readability

import scala.tools.nsc.{Global,Settings}
import scala.tools.nsc.reporters.ConsoleReporter

/* A custom instantiation of the compiler phases up to and including typechecking, followed by our own plugin. */
class PluginRunner(settings : Settings) extends Global(settings, new ConsoleReporter(settings)) {
  val readabilityPlugin = new Plugin(this)

  override protected def computeInternalPhases() {
    val phases = List(
      syntaxAnalyzer          -> "parse source into ASTs, perform simple desugaring",
      analyzer.namerFactory   -> "resolve names, attach symbols to named trees",
      analyzer.packageObjects -> "load package objects",
      analyzer.typerFactory   -> "the meat and potatoes: type the trees",
      superAccessors          -> "add super accessors in traits and nested classes"
      // pickler                 -> "serialize symbol tables",
      // refchecks               -> "reference/override checking, translate nested objects",
      // uncurry                 -> "uncurry, translate function values to anonymous classes",
      // tailCalls               -> "replace tail calls by jumps",
      // specializeTypes         -> "@specialized-driven class and method specialization",
      // explicitOuter           -> "this refs to outer pointers, translate patterns",
      // erasure                 -> "erase types, add interfaces for traits",
      // lazyVals                -> "allocate bitmaps, translate lazy vals into lazified defs",
      // lambdaLift              -> "move nested functions to top level",
      // constructors            -> "move field definitions into constructors",
      // mixer                   -> "mixin composition"
    ).map(_._1) ::: readabilityPlugin.components

    for (phase <- phases) {
      phasesSet += phase
    }
  }
}
