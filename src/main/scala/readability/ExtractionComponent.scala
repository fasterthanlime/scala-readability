package readability

import scala.tools.nsc.{Global,Phase}
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.util.OffsetPosition

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
      val dt = new SnippetFinder(unit)
      dt.run()
    }
  }

  class SnippetFinder(val unit : CompilationUnit) extends Traverser {
    // mutable state is evil but delicious
    var depth : Int = 0

    def run() {
      traverse(unit.body)
    }

    private def puts(s: String) {
      println("  " * depth + s)
    }

    override def traverse(tree : Tree) {
      tree match {
        case v @ ValDef(mods, _, _, rhs) => {
          puts("ValDef (name = %s, type = %s, line = %d" format (v.name, v.symbol.tpe.resultType, v.pos.line))
          
          depth += 1
          traverse(rhs)
          depth -= 1
        }
        case d @ DefDef(mods, _, _, _, _, rhs) => {
          traverse(rhs)
          puts("DefDef (name = %s, line = %d)" format(d.name, d.pos.line))

          depth += 1
          d.children.foreach (x => {
            traverse(x)
          })
          depth -= 1
        }
        case o @ _ => {
          /*
          o.pos match {
            case p: OffsetPosition =>
              puts(o.getClass.getSimpleName + " at line " + p.line)
            case _ =>
              puts(o.getClass.getSimpleName + " " + o.pos.getClass)
          }
          */

          //depth += 1
          super.traverse(tree)
          //depth -= 1
        }
      } 
    }
  }
}
