package com.syedatifakhtar.pipelines

import scala.concurrent.duration.Duration
import scala.language.postfixOps

object Pipelines {

  import scala.concurrent.{Await, ExecutionContext, Future}
  import scala.util.{Failure, Success, Try}


  type StepOutput = Map[String, String]

  trait Context {}

  case class PipelineContext(previousOutput: StepOutput) extends Context

  trait Step[P <: PipelineContext] {
    def name: String
    def run(pc: => P): Try[StepOutput]
  }


  case class UnitStep(override val name: String)
    (task: => PipelineContext => StepOutput)
    extends Step[PipelineContext] {

    override def run(pc: => PipelineContext) = Try {
      task(pc)
    }
  }

  case class ParallelStep(name: String)
    (tasks: => Seq[PipelineContext => StepOutput])
    (implicit val ec: ExecutionContext) extends Step[PipelineContext] {

    import scala.concurrent.duration._


    override def run(pc: => PipelineContext) = {

      val futureTasks = tasks.map {
        task =>
          Future(Try {
            task.apply(pc)
          })(ec)
      }
      val eventualTriedOutputs = Future.sequence(futureTasks)
      val eventualTriedOutput = eventualTriedOutputs.map {
        completedTasks =>
          completedTasks.exists(_.isFailure) match {
            case true => Failure(new Exception("One of the tasks in the Parallel tasks failed"))
            case false =>
              val mergedOutput = completedTasks.map(_.get).reduce(_ ++ _)
              Success(mergedOutput)
          }
      }(ec)
      Await.result(eventualTriedOutput, 10 minutes)
    }
  }


  trait Pipeline {
    val name: String
    val pipelineContext: PipelineContext
    type PipelineOutput = Try[StepOutput]
    def execute: PipelineOutput
    def withOverrides(overrides: => Map[String, String]): Pipeline
  }

  case class SimplePipeline(name: String, pipelineContext: PipelineContext, steps: Seq[Step[PipelineContext]]) extends Pipeline {

    def andThen(step: =>Step[PipelineContext]): SimplePipeline = {
      SimplePipeline(name = name, pipelineContext, steps :+ step)
    }

    def |(step: =>Step[PipelineContext]) = andThen(step)
    def ->(step: => Step[PipelineContext]) = andThen(step)

    protected def execute(step: => Seq[Step[PipelineContext]], pipelineContext: PipelineContext): PipelineContext = {
      if (step.isEmpty) pipelineContext
      else {
        val output = step.head.run(pipelineContext)
        execute(step.tail, pipelineContext = PipelineContext(pipelineContext.previousOutput ++ output.get))
      }
    }

    def execute = {

      Try(execute(steps, pipelineContext).previousOutput)
    }
    override def withOverrides(overrides: => Map[String, String]): Pipeline = {
      SimplePipeline(name = name, PipelineContext(pipelineContext.previousOutput ++ overrides), steps)
    }
  }

  private val nop = Map.empty[String, String]

  object MultiSequencePipeline {
    def empty(name: String) = {
      new MultiSequencePipeline(name, PipelineContext(nop), Seq.empty[Pipeline])
    }
  }

  class MultiSequencePipeline(override val name: String, override val pipelineContext: PipelineContext, pipelines: Seq[Pipeline]) extends Pipeline {
    def andThen(pipeline: =>Pipeline) = new MultiSequencePipeline(name, pipelineContext, pipelines :+ pipeline)
    def |(pipeline: =>Pipeline) = andThen(pipeline)
    def ->(pipeline: =>Pipeline) = andThen(pipeline)
    override def execute: PipelineOutput = {
      def execute(pipelines: Seq[Pipeline], lastOutput: PipelineOutput): PipelineOutput = {
        if (pipelines.isEmpty) lastOutput
        else {
          val output = pipelines.head.withOverrides(lastOutput.get).execute
          execute(pipelines.tail, output)
        }
      }

      execute(pipelines, Try {
        nop
      })
    }
    override def withOverrides(overrides: => Map[String, String]): Pipeline =
      new MultiSequencePipeline(name, PipelineContext(pipelineContext.previousOutput ++ overrides), pipelines)
  }

  case class ForkPipeline(override val name: String, override val pipelineContext: PipelineContext, forks: Seq[Pipeline], timeout: Duration)
    (implicit executionContext: ExecutionContext) extends Pipeline {

    def inParallel(pipeline: => Pipeline) = new ForkPipeline(name, pipelineContext, forks :+ pipeline,timeout)(executionContext)
    def |(pipeline: =>  Pipeline) = inParallel(pipeline)
    def ->(pipeline: => Pipeline) = inParallel(pipeline)
    override def execute: PipelineOutput = {
      println("Forks: " + forks.map(_.name).mkString(";"))
      val eventualOutputs = Future.sequence(forks.map { p =>
        Future {
          p.execute
        }
      })
      val outputs = Await.result(eventualOutputs, timeout)
      val finalOut: StepOutput = outputs.map(_.get).reduce(_ ++ _)
      Try {
        finalOut
      }
    }
    override def withOverrides(overrides: => Map[String, String]): Pipeline =
      new ForkPipeline(name, PipelineContext(pipelineContext.previousOutput ++ overrides),forks,timeout)(executionContext)
  }

  object ForkPipeline {
    def empty(name: String)(duration: Duration)(implicit executionContext: ExecutionContext) = {
      new ForkPipeline(name, PipelineContext(nop), Seq.empty[Pipeline], duration)
    }
  }

  object Pipeline {
    def empty(name: String) = SimplePipeline(name, PipelineContext(nop), Seq.empty[Step[PipelineContext]])
  }

  def main(args: Array[String]): Unit = {
    import scala.concurrent.ExecutionContext
    import ExecutionContext.Implicits.global
    lazy val pipeline =
      Pipeline.empty("Hello World") ->
        UnitStep("storeValue") { pc => Map("a" -> "b") } ->
        UnitStep("printValue")
        { pc => println(pc.previousOutput("a"))
          pc.previousOutput
        } ->
        ParallelStep("Count numbers")(Seq(
          { pc =>
            println("1")
            pc.previousOutput
          },
          { pc =>
            println("1")
            pc.previousOutput
          },
          { pc =>
            println("2")
            pc.previousOutput
          },
          { pc =>
            println("3")
            Map("c" -> "d")
          }
        ))

    val anotherPipeline = Pipeline.empty("Goodbye Pipeline") |
      UnitStep("say Hello") { pc =>
        println("Hello")
        nop
      } |
      UnitStep("Goodbye") {
        pc =>
          println("Goodbye")
          nop
      }

    import scala.concurrent.duration._
    val forkedPipeline = ForkPipeline.empty("Forked Pipeline")(10 minutes) |
      (Pipeline.empty("Count numbers") |
        UnitStep("Count to 100") {
          pc =>
            (1 to 100).map { n =>
              Thread.sleep(100)
              println(n)
              n
            }
            nop
        }) | (Pipeline.empty("say Hello") | UnitStep("Say hello ") {
      pc =>
        (1 to 100).foreach { _ =>
          Thread.sleep(20)
          println("Hello")
        }
        nop
    })

    val joinedPipeline =
      MultiSequencePipeline.empty("HelloGoodbyePipeline") |
        anotherPipeline |
        pipeline |
        forkedPipeline

    val someValue = joinedPipeline.execute
    println(someValue.get)

    Thread.sleep(1000)
    println("Blah")
  }

  val neverRunPipeline =
    Pipeline.empty("NeverRunPipeline") ->
    UnitStep("blah"){
      pc =>
        {throw new RuntimeException("Hello friend!")}
        println("This should never be executed")
        Map.empty[String,String]
    }

}
