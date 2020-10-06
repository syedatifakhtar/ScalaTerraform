package com.syedatifakhtar.scalaterraform

import java.io.File

import TerraformCommand.{MultiValueConfig, MultiValueTerraformArgument, SingleValueTerraformArgument}
import com.syedatifakhtar.scalaterraform.InitArguments.InitArgument
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

}


final case class InitCommand(override val sourceDir: String, override val buildDirPath: String, override val opts: InitArgument*)
  extends TerraformCommand[InitArgument, Unit] with DefaultCmdRunner {
  override val cmd: String = "init"

  private def copyFilesToBuildDir = {
    val buildDirectory = new File(buildDirPath)
    if (!buildDirectory.exists) FileUtils.forceMkdir(buildDirectory)
    FileUtils.copyDirectory(new File(sourceDir), buildDirectory)
  }

  override def commandHook: Unit = copyFilesToBuildDir
}



