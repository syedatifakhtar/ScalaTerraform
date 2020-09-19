package com.scalaterraform.commands

import java.io.File

import com.scalaterraform.commands.TerraformCommand.{MultiValueConfig, MultiValueTerraformArgument, SingleValueTerraformArgument}
import org.apache.commons.io.FileUtils

import scala.reflect.io.Directory

object InitArguments {

  trait InitArgument extends TerraformArgument

  final case class BackendConfigs(
    override val argValue: MultiValueConfig)
    extends InitArgument
      with MultiValueTerraformArgument {
    override protected val argName: String = "backend-config"
  }

  final case class HasBackend(
    override val argValue: String = "true")
    extends InitArgument
      with SingleValueTerraformArgument {
    override protected val argName: String = "backend"
  }

  final case class InitCommand(
    override val sourceDir: String,
    override val buildDirPath: String,
    override val opts: InitArgument*)
    extends TerraformCommand[InitArgument]{
    override val cmd: String = "init"

    private def copyFilesToBuildDir = {
      val buildDirectory = new File(buildDirPath)
      if (!buildDirectory.exists) FileUtils.forceMkdir(buildDirectory)
      val filesDeleted = new Directory(buildDirectory).deleteRecursively()
      if (!filesDeleted) throw new Exception("Files could not be deleted in build dir")
      FileUtils.copyDirectory(new File(sourceDir), buildDirectory)
    }

    override def commandHook: Unit = copyFilesToBuildDir
  }

}

