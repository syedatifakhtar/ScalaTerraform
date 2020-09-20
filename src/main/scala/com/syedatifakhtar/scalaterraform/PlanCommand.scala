package com.syedatifakhtar.scalaterraform

import PlanAndApplyArguments._
import TerraformCommand.{MultiValueConfig, MultiValueTerraformArgument}

final case class PlanCommand(
  override val sourceDir: String,
  override val buildDirPath: String,
  override val opts: PlanArgument*
) extends TerraformCommand[PlanArgument] {
  override val cmd: String = "plan"
}
