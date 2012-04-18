package readability

import scala.tools.nsc.{Global,Phase}
import scala.tools.nsc.plugins.PluginComponent
import scala.tools.nsc.util.{SourceFile,OffsetPosition}
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
      val dt = new LineageFactory(unit)
      val lineage = dt.run()
      lineage.children.foreach(x => x.fix(unit.source))
      lineage.print()
    }
  }

  class Node(val tree: Tree) {
    var minLine: Int = tree.pos.line 
    var maxLine: Int = tree.pos.line 
    var children: List[Node] = Nil

    def fix(source: SourceFile) {
       val totalLines = source.offsetToLine(source.length - 1)
       println("In " + source + ", total lines = " + totalLines)
       while(balance(source) != 0 && maxLine < totalLines) {
         maxLine += 1 
       }
    }

    def balance(source: SourceFile): Int = {
        val content = ((minLine - 1) until maxLine).map(source.lineToString _).foldLeft("")(_ + _)
        val braces = content.replaceAll("[^{}\\(\\)\\[\\]]", "")
        val opening = braces.replaceAll("[^{\\(\\[]", "")
        val closing = braces.replaceAll("[^}\\)\\]]", "")
        val result = opening.length - closing.length

        println(minLine + "-" + maxLine + ", braces = " + braces + ", balance = " + result)
        result
    }

    def enlarge(line: Int) {
        if (line < minLine) minLine = line
        if (line > maxLine) maxLine = line
    }

    def print() {
        print0(0)
    }

    private def puts(level: Int, s: String) {
      println("  " * level + s)
    }

    private def print0(level: Int) {
        tree match {
          case v @ ValDef(mods, _, _, rhs) => {
            //puts(level, "val %s: %s     lines %d-%d" format (v.name, v.symbol.tpe.resultType, minLine, maxLine))
          }
          case d @ DefDef(mods, _, _, _, _, rhs) => {
            puts(level, "def %s(...)    lines %d-%d" format(d.name, minLine, maxLine))
          }
          case _ => {}
        }

        children.foreach(x => x.print0(level + 1))
    }
  }

  class LineageFactory(val unit : CompilationUnit) extends Traverser {
    // mutable state is evil but delicious
    var stack = new Stack[Node].push(new Node(unit.body))

    def run() : Node = {
      traverse(unit.body)
      stack.top
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
          withNode(new Node(v), {
            traverse(rhs)
          })
        }
        case d @ DefDef(mods, _, _, _, _, rhs) => {
          withNode(new Node(d), {
            traverse(rhs)
            d.children.foreach (x => {
              traverse(x)
            })
          })
        }
        case o @ _ => {
          o.pos match {
            case p: OffsetPosition =>
              enlarge(p.line)
            case _ =>
              { /* Tough luck! */ }
          }

          super.traverse(tree)
        }
      } 
    }
  }
}
