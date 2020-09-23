package com.syedatifakhtar.scalaterraform


import java.io.File

import com.syedatifakhtar.scalaterraform.DestroyArguments.DestroyArgument
import com.syedatifakhtar.scalaterraform.InitArguments.InitArgument
import com.syedatifakhtar.scalaterraform.PlanAndApplyArguments.{ApplyArgument, PlanArgument}
import com.typesafe.scalalogging.LazyLogging

import scala.reflect.io.Directory
import scala.util.Try

trait ArgsResolver {
  def getInitArgs(): Seq[InitArgument]
  def getPlanArgs(): Seq[PlanArgument]
  def getApplyArgs(): Seq[ApplyArgument]
  def getDestroyArgs(): Seq[DestroyArgument]
}

case class TerraformModule(path: String, buildPath: String, nameInConfig: String)
  (argsResolver: ArgsResolver) extends LazyLogging {

  def init = {
    for {
      _ <- Try {
        val directory = new Directory(new File(buildPath))
        logger.debug("Deleting " + buildPath)
        if (!buildPath.contains("build")) {
          throw new Exception("Build path does not contain `build` in the path string," +
            " this is to avoid accidental deletion when wrong build path args are passed")
        }
        directory.deleteRecursively()
      }
      result <- InitCommand(path, buildPath, argsResolver.getInitArgs(): _*).run
    } yield result
  }

  def apply = {
    for {
      _ <- init
      result <- ApplyCommand(path, buildPath, argsResolver.getApplyArgs(): _*).run
    } yield result
  }
  def plan = {
    for {
      _ <- init
      result <- PlanCommand(path, buildPath, argsResolver.getPlanArgs(): _*).run
    } yield result
  }
  def destroy = {
    for {
      _ <- init
      result <- DestroyCommand(path, buildPath, argsResolver.getDestroyArgs(): _*).run
    } yield result
  }
  def output = {
    for {
      result <- OutputCommand(path, buildPath).run
    } yield result
  }
}
