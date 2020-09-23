package com.syedatifakhtar.scalaterraform


import com.syedatifakhtar.scalaterraform.OutputArguments.OutputArguments

object OutputArguments {

  trait OutputArguments extends TerraformArgument

}

final case class OutputCommand(
  override val sourceDir: String,
  override val buildDirPath: String,
  override val opts: OutputArguments*
) extends TerraformCommand[OutputArguments,Map[String,String]] with CaputureOutputCmdRunner  {
  override val cmd: String = "output"
  override val prependCondition: String = "-json"
}
