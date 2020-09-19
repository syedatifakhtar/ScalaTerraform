package com.scalaterraform.commands

import java.io.File
import scala.reflect.io.Directory

import com.typesafe.scalalogging.Logger
import org.apache.commons.io.FileUtils

import scala.util.Try


trait TerraformArgument {
  protected val argName: String
  def validation: () => Boolean = () => true
  def buildParamOutput: String
  def defaultParams = ""

}

object TerraformCommand {
  type MultiValueConfig = Map[String, String]

  trait MultiValueTerraformArgument extends TerraformArgument {
    val argValue: MultiValueConfig

    def buildParamOutput: String =
      s"""${argValue.map(v => s"-${argName}=" + s"'${v._1}=${v._2}'").mkString(" ")}
         | $defaultParams
    """.stripMargin.trim
  }

  trait SingleValueTerraformArgument extends TerraformArgument {
    val argValue: String

    def buildParamOutput: String = s"$defaultParams -${argName}=${argValue}".trim
  }

}

protected trait TerraformCommand[T <: TerraformArgument] {
  protected val LOG: Logger = Logger.apply(this.getClass)

  import scala.sys.process.{ProcessLogger, stderr, stdout}
  import scala.sys.process._

  private val processLogger = ProcessLogger(stdout append _, stderr append _)
  protected def runCommand = {
    (cmd: String, absolutePath: String) =>
      LOG.debug(s"Running command-> ${cmd} in Dir-> ${absolutePath}")
      val exitCode = Process(Seq("bash","-c",cmd), Some(new File(absolutePath))) ! processLogger
      stdout.println()
      if (exitCode != 0) {
        stderr.println()
        throw new Exception(s"Failed to run command : ${cmd}")
      }
  }
  val opts: Seq[T]
  val sourceDir: String
  val buildDirPath: String
  protected val cmd: String
  def prependCondition = ""
  def validateAll(): Unit = {
    opts.foreach {
      opt => if (!opt.validation.apply()) throw new Exception(s"Validation failed for argument")
    }
  }

  def buildCommand: String = {
    val stringBuilder = new StringBuilder
    stringBuilder.append(s"terraform $cmd")
    if (prependCondition.nonEmpty) stringBuilder.append(s" $prependCondition")
    if (opts.nonEmpty) stringBuilder.append(" " + opts.map(_.buildParamOutput).mkString(" "))
    stringBuilder.toString
  }

  def commandHook = {}
  def run: Try[Unit] = {
    Try {
      if(!buildDirPath.contains("build")) throw new Exception("Build directory must have build in the path to prevent accidental deletion")
      validateAll
      commandHook
      val command = buildCommand
      LOG.info(s"Running command: $command")
      runCommand(command, buildDirPath)
    }
  }
}
