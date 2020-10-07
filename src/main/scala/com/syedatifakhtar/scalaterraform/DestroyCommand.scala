package com.syedatifakhtar.scalaterraform


import TerraformCommand.{MultiValueConfig, MultiValueTerraformArgument}
import com.syedatifakhtar.scalaterraform.DestroyArguments.DestroyArgument

object DestroyArguments {

  trait DestroyArgument extends TerraformArgument

  final case class Vars(
    override val argValue: MultiValueConfig)
    extends DestroyArgument
      with MultiValueTerraformArgument {
    override protected val argName: String = "var"
  }

}

final case class DestroyCommand(
  override val sourceDir: String,
  override val buildDirPath: String,
  override val opts: DestroyArgument*
) extends TerraformCommand[DestroyArgument,Unit] with DefaultCmdRunner  {
  override val cmd: String = TerraformCommands.DESTROY
  override val prependCondition: String = "-auto-approve"
}
