package readability

import scala.tools.nsc.{Global,Settings,CompilerCommand}

object Main {
  def main(args : Array[String]) {
    val settings = new Settings
    val command = new CompilerCommand(args.toList, settings) {
      override val cmdName = "scalac-readability"
    }

    if(command.ok) {
      val runner = new PluginRunner(settings)
      val run = new runner.Run
      run.compile(command.files)
    }
  }
}
