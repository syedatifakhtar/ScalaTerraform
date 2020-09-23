package com.syedatifakhtar.scalaterraform

import java.io.{ByteArrayOutputStream, File, PrintWriter}

import com.typesafe.scalalogging.{LazyLogging, Logger}

import scala.util.Try


trait TerraformArgument extends Product with Serializable{
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

import scala.sys.process.{ProcessLogger, _}

protected trait CommandRunner[OUT] {
  val stdoutStream = new ByteArrayOutputStream
  val stderrStream = new ByteArrayOutputStream
  val stdoutWriter = new PrintWriter(stdoutStream)
  val stderrWriter = new PrintWriter(stderrStream)
  protected val processLogger = ProcessLogger(stdoutWriter.println, stderrWriter.println)
  def runCommand(cmd: String, path: String)(implicit logger: Logger): OUT
}

trait DefaultCmdRunner extends CommandRunner[Unit] {

  override def runCommand(cmd: String, absolutePath: String)(implicit logger: Logger) = {
    logger.info(s"Running command-> ${cmd} in Dir-> ${absolutePath}")
    val exitCode = Process(Seq("bash", "-c", cmd), Some(new File(absolutePath))) ! processLogger
    stdoutWriter.close()
    stderrWriter.close()
    logger.info(stdoutStream.toString)
    logger.info(stderrStream.toString)
    if (exitCode != 0) {
      throw new Exception(s"Failed to run command : ${cmd}")
    }
  }
}

trait CaputureOutputCmdRunner extends CommandRunner[Map[String, String]] {

  import play.api.libs.json._

  override def runCommand(cmd: String, absolutePath: String)(implicit logger: Logger): Map[String, String] = {
    logger.info(s"Running command-> ${cmd} in Dir-> ${absolutePath}")
    val exitCode = Process(Seq("bash", "-c", cmd), Some(new File(absolutePath))) ! processLogger
    stdoutWriter.close()
    stderrWriter.close()
    if (exitCode != 0) {
      throw new Exception(s"Failed to run command : ${cmd}")
    }
    val outputResult = stdoutStream.toString
    logger.info(s"Captured output: $outputResult")
    Json.parse(outputResult).as[Map[String, JsValue]].map { case (k, v) => (k, (v \ "value").as[String]) }
  }
}

protected trait TerraformCommand[T <: TerraformArgument, OUT] extends LazyLogging {

  commandRunner: CommandRunner[OUT] =>
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
  def run: Try[OUT] = {
    Try {
      if (!buildDirPath.contains("build")) throw new Exception("Build directory must have build in the path to prevent accidental deletion")
      validateAll
      commandHook
      val command = buildCommand
      logger.info(s"Running command: $command")
      runCommand(command, buildDirPath)(logger)
    }
  }
}
