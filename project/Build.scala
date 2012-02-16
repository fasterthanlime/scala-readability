import sbt._
import Process._
import Keys._

object ScalaReadability extends Build {
  def scriptFile = file(".") / "scalac-readability"

  val scriptTask = TaskKey[Unit]("script", "Generate scalac-readability") <<= (streams, dependencyClasspath in Compile, classDirectory in Compile) map { (s, deps, out) =>
    if(!scriptFile.exists) {
      s.log.info("Generating script...")
      try {
        val nl = System.getProperty("line.separator")
        val fw = new java.io.FileWriter(scriptFile)
        fw.write("#!/bin/bash --posix" + nl)
        val depsPaths = deps.map(_.data.absolutePath)
        fw.write("SCALACLASSPATH=\"")
        fw.write((out.absolutePath +: depsPaths).mkString(":"))
        fw.write("\"" + nl + nl)
        fw.write("JAVA_OPTS=\"-Xmx2G -Xms512M\" scala -classpath ${SCALACLASSPATH} \\" + nl)
        fw.write("  readability.Main $@" + nl)
        fw.close
        scriptFile.setExecutable(true)
      } catch {
        case e => s.log.error("There was an error while generating the script file: " + e.getLocalizedMessage)
      }
    }
  }

  private val nameKey = SettingKey[String]("name", "Name of the project")
  private val scalaVersionKey = SettingKey[String]("scalaVersion", "Scala Version")
  private val versionKey = SettingKey[String]("version", "Version")

  object ReadabilityProject {
    val settings = Seq(
      scriptTask,
      nameKey := "Scala Readability",
      scalaVersionKey := "2.9.1",
      versionKey := "1.0.0"
    )
  }

  lazy val root = Project(
    id = "scalac-readability",
    base = file("."),
    settings = Project.defaultSettings ++ ReadabilityProject.settings
  )
}
