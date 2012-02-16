package readability

import scala.tools.nsc.{Global,Settings,CompilerCommand}

object Main {
  def main(args : Array[String]) {
    val settings = new Settings

    // Somehow, these options don't get passed by default, so we work our way around it.
    val pluginOptionPrefix = "-P:readability:"
    val (pluginSettings,scalacSettings) = args.toList.partition(_.startsWith(pluginOptionPrefix))

    val command = new CompilerCommand(scalacSettings, settings) {
      override val cmdName = "scalac-readability"
    }

    lazy val runner = new PluginRunner(settings)
    if(!command.ok) {
      Console.err.println("\n" + command.shortUsage)
    } else if(command.shouldStopWithInfo) {
      Console.err.println(command.getInfoMessage(runner))
      runner.readabilityPlugin.optionsHelp.foreach(msg => Console.err.println(msg))
    } else {
      // we still need to process the plugin-specific options
      runner.readabilityPlugin.processOptions(pluginSettings.map(s => s.substring(pluginOptionPrefix.length, s.length)), Console.err.println(_))
      val run = new runner.Run
      run.compile(command.files)
    }
  }
}
