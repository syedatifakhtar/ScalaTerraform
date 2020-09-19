package com.scalaterraform.commands

import com.scalaterraform.commands.PlanAndApplyArguments._
import com.scalaterraform.commands.TerraformCommand.{MultiValueConfig, MultiValueTerraformArgument}

final case class PlanCommand(
  override val sourceDir: String,
  override val buildDirPath: String,
  override val opts: PlanArgument*
) extends TerraformCommand[PlanArgument] {
  override val cmd: String = "plan"
}
