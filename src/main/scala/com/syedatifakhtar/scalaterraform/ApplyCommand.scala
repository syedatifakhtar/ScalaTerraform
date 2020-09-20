package com.syedatifakhtar.scalaterraform

import PlanAndApplyArguments._
import TerraformCommand.{MultiValueConfig, MultiValueTerraformArgument}

object PlanAndApplyArguments {

  trait ApplyArgument extends TerraformArgument
  trait PlanArgument extends TerraformArgument

  final case class Vars(
    override val argValue: MultiValueConfig)
    extends ApplyArgument
      with PlanArgument
      with MultiValueTerraformArgument {
    override protected val argName: String = "var"
  }

}

final case class ApplyCommand(
  override val sourceDir: String,
  override val buildDirPath: String,
  override val opts: ApplyArgument*
) extends TerraformCommand[ApplyArgument] {
  override val cmd: String = "apply"
  override val prependCondition: String = "-auto-approve"
}
