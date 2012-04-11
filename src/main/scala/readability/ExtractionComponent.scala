package readability

import scala.tools.nsc.{Global,Phase}
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.util.OffsetPosition
import scala.collection.immutable.Stack

abstract class ExtractionComponent(plugin : Plugin) extends PluginComponent {
  val global : Global // provided at instantiation time
  import global._

  override val runsRightAfter : Option[String] = Some("typer")
  override val runsAfter : List[String]        = List("typer")

  val phaseName = plugin.name

  def newPhase(previous : Phase) = new ExtractionPhase(previous)

  class ExtractionPhase(previous : Phase) extends StdPhase(previous) {
    def apply(unit : CompilationUnit) : Unit = {
      println("In compilation unit " + unit + ".")
      val dt = new SnippetFinder(unit)
      dt.run()
    }
  }

  class Node(val tree: Tree) {
    var minLine: Int = tree.pos.line 
    var maxLine: Int = tree.pos.line 
    var children: List[Node] = Nil

    def enlarge(line: Int) {
        if (line < minLine) minLine = line
        if (line > maxLine) maxLine = line
    }
  }

  class SnippetFinder(val unit : CompilationUnit) extends Traverser {
    // mutable state is evil but delicious
    var stack = new Stack[Node].push(new Node(unit.body))

    def run() {
      traverse(unit.body)
    }

    /*
     * println with automatic indentation according to the
     * stack state.
     */
    private def puts(s: String) {
      println("  " * stack.size + s)
    }

    /*
     * This takes care of the stack elegantly: basically,
     * it makes sure you don't forget to pop.
     */
    private def withNode(n: Node, f: => Unit) {
        val parent = stack.top
        parent.children =  parent.children :+ n

        stack = stack.push(n)
        f
        stack = stack.pop
    }

    /*
     * Adjust all nodes according to the line of the current element.
     */
    private def enlarge(line: Int) {
        stack.foreach(_.enlarge(line))
    }

    override def traverse(tree : Tree) {
      tree match {
        case v @ ValDef(mods, _, _, rhs) => {
          puts("val %s: %s          line %d" format (v.name, v.symbol.tpe.resultType, v.pos.line))
          
          withNode(new Node(v), {
            traverse(rhs)
          })
        }
        case d @ DefDef(mods, _, _, _, _, rhs) => {
          puts("def %s(...)         line %d" format(d.name, d.pos.line))

          withNode(new Node(d), {
            traverse(rhs)
            d.children.foreach (x => {
              traverse(x)
            })
          })
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

          super.traverse(tree)
        }
      } 
    }
  }
}
