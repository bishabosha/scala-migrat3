package migrate

import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin

object ScalaMigratePlugin extends AutoPlugin {

  object autoImport {
    val scala3CompilerOptions = taskKey[Seq[String]]("scalacOptions for scala 3")
    val checkRequirements     = taskKey[Unit]("check requirements")
    val migrate               = taskKey[Unit]("migrate a specific project to scala 3")
  }
  import autoImport._

  override def requires: Plugins = JvmPlugin

  override def trigger = noTrigger // Need to be manually activated.

  override def globalSettings: Seq[Def.Setting[_]] = Seq(
    scala3CompilerOptions := Nil
  )

  lazy val migrate = Def.task {
    val log = streams.value.log
    log.info("we are going to migrate your project to scala 3 maybe")

    val input                 = (Compile / sourceDirectory).value
    val workspace             = (ThisBuild / baseDirectory).value
    val scala2Classpath       = (Compile / fullClasspath).value
    val scala2CompilerOptions = (Compile / scalacOptions).value
    val semanticdbPath        = (Compile / semanticdbTargetRoot).value

    // computed values
    val scalac3Options  = scala3CompilerOptions.value
    val scala3Classpath = ???

    // hardcoded values ..
    val toolClasspath = ""

  }

  /*
    "input"     -> (input / Compile / sourceDirectory).value,
    "output"    -> (output / Compile / sourceDirectory).value,
    "workspace" -> (ThisBuild / baseDirectory).value,
    fromClasspath("scala2Classpath", input / Compile / fullClasspath),
    "semanticdbPath" -> (input / Compile / semanticdbTargetRoot).value,
    fromScalacOptions("scala2CompilerOptions", input / Compile / scalacOptions),
    fromClasspath("toolClasspath", `scalafix-rules` / Compile / fullClasspath),
    fromClasspath("scala3Classpath", output / Compile / fullClasspath),
    fromScalacOptions("scala3CompilerOptions", output / Compile / scalacOptions),
    "scala3ClassDirectory" -> (output / Compile / compile / classDirectory).value
   */

}
