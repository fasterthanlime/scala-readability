package readability

import scala.tools.nsc.Global
import scala.tools.nsc.plugins.{Plugin=>NSCPlugin,PluginComponent}

class Plugin(val global : Global) extends NSCPlugin {
  val name = "scala-readability"
  val description = "Snippet extraction for the readability study"

  val mainComponent = new ExtractionComponent(this) {
    // path-dependent type trickery to ensure the type of global really is the same here and in the component instance.
    val global : Plugin.this.global.type = Plugin.this.global
  }

  val components = List[PluginComponent](mainComponent)

  // This adds information to the output of "scalac -help" when the plugin is used.
  override val optionsHelp : Option[String] = Some(
    "  -P:"+name+":nyan             Displays a cat flying over a rainbow\n"
  )

  // This is invoked by the compiled for all -P:scala-readability:* options.
  // Only the * part is received.
  override def processOptions(options : List[String], error : String=>Unit) {
    for(option <- options) option match {
      case "nyan" => {
        import java.awt.Desktop
        import java.net.URI

        try {
          Desktop.getDesktop.browse(new URI("http://nyan.cat"))
        } finally {
          sys.exit(0)
        } 
      }
      case _ => error("Don't know what to do with option : -P:"+name+":"+option+".")
    }
  }
}
