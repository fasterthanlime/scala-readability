package readability

import scala.tools.nsc.{Global,Phase}
import scala.tools.nsc.plugins.PluginComponent

abstract class ExtractionComponent(plugin : Plugin) extends PluginComponent {
  val global : Global // provided at instantiation time
  import global._

  override val runsRightAfter : Option[String] = Some("typer")
  override val runsAfter : List[String]        = List("typer")

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
          println("  - is range? : " + v.pos.isRange)
        }
        case d @ DefDef(mods, _, _, _, _, rhs) => {
          traverse(rhs)
          println("Found a method definition : ")
          println("  - name : " + d.name)
          println("  - pos  : " + d.pos.toString)
          println("  - is range? : " + d.pos.isRange)
          println("  - rhspos  : " + d.rhs.pos.toString)
          println("  - is range? : " + d.rhs.pos.isRange)
          val lastChild = d.children.last
          println("  - last child pos  : " + lastChild.pos.toString)
          println("  - is range? : " + lastChild.pos.isRange)
        }
        case o @ _ => {
          // println("Found a " + o.getClass)
          super.traverse(tree)
        }
      } 
    }
  }
}
