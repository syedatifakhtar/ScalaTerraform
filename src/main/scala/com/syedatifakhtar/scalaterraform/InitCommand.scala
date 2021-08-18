package com.syedatifakhtar.scalaterraform

import java.io.File
import TerraformCommand.{MultiValueConfig, MultiValueTerraformArgument, SingleValueTerraformArgument}
import com.syedatifakhtar.scalaterraform.InitArguments.InitArgument
import com.syedatifakhtar.utils.FileResourceUtil
import org.apache.commons.io.FileUtils

import java.net.JarURLConnection
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
  override val cmd: String = TerraformCommands.INIT

  private def copyFilesToBuildDir = {
    val buildDirectory = new File(buildDirPath)
    if(sourceDir.contains(".jar")) {
      logger.debug("Starting copying terraform files from Jar")
      val file = this.getClass.getResource(sourceDir.split(".jar!")(1))
      val connection = file.openConnection().asInstanceOf[JarURLConnection]
      FileResourceUtil.copyJarResourcesRecursively(buildDirectory,connection)
    } else {
      logger.debug("Copying regular files to dir...")
      if (!buildDirectory.exists) FileUtils.forceMkdir(buildDirectory)
      FileUtils.copyDirectory(new File(sourceDir), buildDirectory)
    }
    logger.debug(s"Completed copying files to path: ${buildDirPath}")
  }

  override def commandHook: Unit = copyFilesToBuildDir
}



