package readability

import scala.tools.nsc.{Global,Phase}
import scala.tools.nsc.plugins.PluginComponent

abstract class ExtractionComponent(plugin : Plugin) extends PluginComponent {
  val global : Global // provided at instantiation time
  import global._

  override val runsRightAfter : Option[String] = Some("refchecks")
  override val runsAfter : List[String]        = List("refchecks")

  val phaseName = plugin.name

  def newPhase(previous : Phase) = new ExtractionPhase(previous)

  class ExtractionPhase(previous : Phase) extends StdPhase(previous) {
    def apply(unit : CompilationUnit) : Unit = {
      println("The phase is running on compilation unit " + unit + ".")
      val dt = new DemoTraverser(unit)
      dt.run()
    }
  }

  class DemoTraverser(val unit : CompilationUnit) extends Traverser {
    def run() {
      traverse(unit.body)
    }

    override def traverse(tree : Tree) {
      tree match {
        case v @ ValDef(mods, _, _, rhs) => {
          traverse(rhs)
          println("Found a" + (if(mods.isMutable) " " else "n im") + "mutable variable definition : ")
          println("  - name : " + v.name)
          println("  - type : " + v.symbol.tpe.resultType)
          println("  - pos  : " + v.pos.toString)
        }
        case o @ _ => {
          // println("Found a " + o.getClass)
          super.traverse(tree)
        }
      } 
    }
  }
}
