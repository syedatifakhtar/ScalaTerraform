package com.syedatifakhtar.scalaterraform


import java.io.File

import com.syedatifakhtar.scalaterraform.DestroyArguments.DestroyArgument
import com.syedatifakhtar.scalaterraform.InitArguments.{BackendConfigs, HasBackend, InitArgument}
import com.syedatifakhtar.scalaterraform.PlanAndApplyArguments.{ApplyArgument, PlanArgument}
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.io.Directory
import scala.util.{Failure, Try}

trait ArgsResolver {
  def getInitArgs(): Seq[InitArgument]
  def getPlanArgs(): Seq[PlanArgument]
  def getApplyArgs(): Seq[ApplyArgument]
  def getDestroyArgs(): Seq[DestroyArgument]
}

case class DefaultConfigArgsResolver[ConfigType]
(configValueResolver: String => Option[Map[String, String]])
  (configTree: String)
  (nameInConfig: String)
  extends ArgsResolver {

  private val vars = configValueResolver(s"${configTree}.${nameInConfig}.vars")
  private val backendConfigs = configValueResolver(s"${configTree}.${nameInConfig}.backend-config")
  override def getInitArgs(): Seq[InitArgument] = {
    if (backendConfigs.isDefined)
      Seq(HasBackend(), BackendConfigs(backendConfigs.get))
    else Seq.empty
  }
  override def getPlanArgs(): Seq[PlanArgument] = {
    if (vars.isDefined)
      Seq(PlanAndApplyArguments.Vars(vars.get)) else Seq.empty
  }
  override def getApplyArgs(): Seq[ApplyArgument] = {
    if (vars.isDefined)
      Seq(PlanAndApplyArguments.Vars(vars.get)) else Seq.empty
  }
  override def getDestroyArgs(): Seq[DestroyArgument] = {
    if (vars.isDefined)
      Seq(DestroyArguments.Vars(vars.get)) else Seq.empty
  }
}


case class TerraformModule(sourcePath: String, buildPath: String)(val moduleName: String)
  (argsResolver: ArgsResolver) extends LazyLogging {


  def finalSourcePath = s"${sourcePath}/$moduleName"
  def finalBuildPath = s"${buildPath}/$moduleName"
  def invoke(cmd: String) = {
    cmd match {
      case "init" => init
      case "apply" => apply
      case "plan" => plan
      case "destroy" => destroy
      case "output" => output
      case _ => Failure(new Exception("Invalid command"))
    }
  }
  def init: Try[Unit] = {
    for {
      _ <- Try {
        val directory = new Directory(new File(finalBuildPath))
        if (directory.exists) {
          logger.debug("Deleting " + finalBuildPath)
          if (!finalBuildPath.contains("build")) {
            throw new Exception("Build path does not contain `build` in the path string," +
              " this is to avoid accidental deletion when wrong build path args are passed")
          }
          directory.deleteRecursively()
        }
      }
      result <- InitCommand(finalSourcePath, finalBuildPath, argsResolver.getInitArgs(): _*).run
    } yield result
  }

  def apply: Try[Unit] = {
    for {
      _ <- init
      result <- ApplyCommand(finalSourcePath, finalBuildPath, argsResolver.getApplyArgs(): _*).run
    } yield result
  }
  def plan = {
    for {
      _ <- init
      result <- PlanCommand(finalSourcePath, finalBuildPath, argsResolver.getPlanArgs(): _*).run
    } yield result
  }
  def destroy = {
    for {
      _ <- init
      result <- DestroyCommand(finalSourcePath, finalBuildPath, argsResolver.getDestroyArgs(): _*).run
    } yield result
  }
  def output = {
    for {
      result <- OutputCommand(finalSourcePath, finalBuildPath).run
    } yield result
  }
}
