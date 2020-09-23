package com.syedatifakhtar.scalaterraform

import com.syedatifakhtar.scalaterraform.PlanAndApplyArguments._

final case class PlanCommand(
  override val sourceDir: String,
  override val buildDirPath: String,
  override val opts: PlanArgument*)
  extends TerraformCommand[PlanArgument, Unit] with DefaultCmdRunner {
  override val cmd: String = "plan"
}
