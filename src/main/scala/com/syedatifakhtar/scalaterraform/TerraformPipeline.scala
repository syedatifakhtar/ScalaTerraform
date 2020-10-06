package com.syedatifakhtar.scalaterraform

import scala.util.Try

object TerraformPipeline {

  import com.syedatifakhtar.pipelines.Pipelines._


  case class TerraformStep(module: TerraformModule)(command: String) extends Step[PipelineContext] {
    override val name = module.moduleName
    override def run(pc: PipelineContext): Try[StepOutput] = {
      module.invoke(command) map {
        case Some(y: Map[String, String]) => y
        case _ => Map.empty[String, String]
      }
    }
  }

  class TerraformPipeline(override val name: String
    , terraformCommand: String
    , override val steps: Seq[TerraformStep]
    , overrides: Map[String, String]
  )
    extends SimplePipeline(name, PipelineContext(overrides), steps) {

    def andThen(invokableStep: String => TerraformStep) = {
      new TerraformPipeline(name, terraformCommand, steps :+ invokableStep(terraformCommand), overrides)
    }
    def |(invokableStep: String => TerraformStep) = {
      andThen(invokableStep)
    }
    def ->(invokableStep: String => TerraformStep) = {
      andThen(invokableStep)
    }
  }

  object TerraformPipeline {
    def empty(name: String, terraformCommand: String) =
      new TerraformPipeline(name, terraformCommand, Seq.empty, Map.empty)
  }

}
