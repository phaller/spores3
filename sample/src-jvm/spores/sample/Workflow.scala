package spores.sample

import upickle.default.*

import spores.default.*
import spores.default.given
import spores.sample.platform.*


object WorkflowMain {
  import Workflow.*
  import Workflow.given

  // Custom error type for our workflow
  case class Error(message: String) derives ReadWriter
  // Custom Spore[ReadWriter[Error]] for our error type
  given Spore[ReadWriter[Error]] = Spore.auto(summon)


  def main(args: Array[String]): Unit = {

    // A custom workflow which processes an input integer and returns an
    // Either[Error, Int] result. It will check if the input is positive and
    // even, and add 12 to it if both conditions are met.
    def workflow(input: Either[Error, Int]) =
      Workflow
        .apply[Int, Error](input)
        .map { x =>
          if (x >= 0) then Right(x) else Left(Error(s"Value $x is not positive"))
        }
        .map { x =>
          if (x % 2) == 0 then Right(x) else Left(Error(s"Value $x is not even"))
        }
        .map { x =>
          Right(x + 12)
        }

    // Workflows with different inputs
    val workflow6 = workflow(Right(6))
    val workflow7 = workflow(Right(7))
    val workflowNeg = workflow(Right(-1))
    val workflowErr = workflow(Left(Error("Initial error")))

    // Run each workflows. This will create a new JSON file prefixed with the
    // step number for each intermediate step. And return the final result.
    val result6   = Interpreter.run(workflow6,   Some("workflow6.json"))
    val result7   = Interpreter.run(workflow7,   Some("workflow7.json"))
    val resultNeg = Interpreter.run(workflowNeg, Some("workflowNeg.json"))
    val resultErr = Interpreter.run(workflowErr, Some("workflowErr.json"))

    // Print and assert the results
    assert(result6 == Right(18))
    println(s"Result for workflow6: $result6")

    assert(result7 == Left(Error("Value 7 is not even")))
    println(s"Result for workflow7: $result7")

    assert(resultNeg == Left(Error("Value -1 is not positive")))
    println(s"Result for workflowNeg: $resultNeg")

    assert(resultErr == Left(Error("Initial error")))
    println(s"Result for workflowErr: $resultErr")

    // We can load one of the intemediate steps from a file and run it again.
    // We can load one of the intermediate steps from a file. Here we load
    // workflow7 step 2, which is the step just before the last map operation.
    val loaded = readFromFile[Workflow[Nothing, Int, Error]]("2-workflow7.json")

    println(s"Loaded workflow7 from step 2: $loaded")

    // Let's run it.
    val resultLoaded = Interpreter.run(loaded, Some("loaded.json"))

    assert(resultLoaded == Left(Error("Value 7 is not even")))
    println(s"Result for loaded workflow: $resultLoaded")
  }
}


/** Transforms an `Inp`ut to an `Out`put by repeated application of [[map]]. 
  * `Err`ors are propagated. The resulting value of running the workflow is
  * `Either[Err, Out]`.
  * 
  * See [[Workflow.apply]] for creating a workflow from an initial value.
  *
  * See [[Workflow.Interpreter.run]] for running a workflow.
  */
sealed trait Workflow[-Inp, +Out, Err] {
  import Workflow.*

  inline def map[OutB](inline f: Out => Either[Err, OutB])(using rw: Spore[ReadWriter[Either[Err, OutB]]]): Workflow[Inp, OutB, Err] = {
    val spore = Spore.auto(f)
    Map(this, spore, rw)
  }
}


object Workflow {
  // ReadWriter instance of Workflow. This way it can be serialized.
  given [Inp, Out, Err]: ReadWriter[Workflow[Inp, Out, Err]] = macroRW

  // The initial or current value of the workflow.
  private case class Value[Out, Err](
      value: Spore[Either[Err, Out]]
  ) extends Workflow[Nothing, Out, Err]
      derives ReadWriter

  private object Value {
    given [Out, Err]: ReadWriter[Value[Out, Err]] = macroRW
  }

  // A `map` operation, applying the function in the `spore` to the result of
  // the `prev`ious step. The `rw` is needed to serialize the intermediate
  // result.
  private case class Map[Inp, Tmp, Out, ErrTmp, Err](
      prev: Workflow[Inp, Tmp, ErrTmp],
      spore: Spore[Tmp => Either[Err, Out]],
      rw: Spore[ReadWriter[Either[Err, Out]]]
  ) extends Workflow[Inp, Out, Err]
      derives ReadWriter

  private object Map {
    given [Inp, Tmp, Out, ErrTmp, Err]: ReadWriter[Map[Inp, Tmp, Out, ErrTmp, Err]] = macroRW
  }

  /** Create a workflow starting from the initial `value`. */
  def apply[Out, Err](value: Either[Err, Out])(using Spore[ReadWriter[Out]], Spore[ReadWriter[Err]]): Workflow[Nothing, Out, Err] = {
    val spore = Spore.auto(value)
    Value(spore)
  }

  object Interpreter {

    /** Transforms the `workflow` by a single step.
      * 
      * If the provided `workflow` is a `Value(value)`, then this `Value(value)`
      * is returned. Otherwise, the `Map` operation closest to the value is
      * applied and replaced with the resulting `Value(result)`.
      */
    private def step[Inp, Out, Err](workflow: Workflow[Inp, Out, Err]): Workflow[Inp, Out, Err] = workflow match {
      // Do nothing
      case Value(v) =>
        Value(v)
      // Apply the spore to the previous value and return the result as a Value.
      case Map(prev @ Value(value), spore, rw) =>
        value.unwrap() match
          case Right(v) =>
            spore.unwrap()(v) match
              case right @ Right(out) => Value(Env(right)(using rw))
              case left @ Left(err)   => Value(Env(left)(using rw))
          // Propagate the error
          case Left(_) => prev.asInstanceOf[Workflow[Inp, Out, Err]]
      // Recursive application
      case Map(prev, spore, rw) =>
        Map(step(prev), spore, rw)
    }

    /** Run the `workflow` and returns the result as an `Either[Err, Out]`.
      * If some `fileSuffix` is provided, then each intermediate step is written
      * to the file with its step index as a prefix.
      */
    def run[Inp, Out, Err](workflow: Workflow[Inp, Out, Err], fileSuffix: Option[String] = None): Either[Err, Out] = {

      // Write if fileSuffix is provided
      def maybeWrite(workflow: Workflow[Nothing, Any, Err], fileSuffix: Option[String], stepIndex: Int): Unit = {
        if fileSuffix.isDefined then
          val filename = stepIndex + "-" + fileSuffix.get
          writeToFile(workflow, filename)
      }

      // Recursive run
      def rec[Inp, Out](inter: Workflow[Inp, Out, Err], fileSuffix: Option[String], stepIndex: Int): Either[Err, Out] = {
        maybeWrite(inter, fileSuffix, stepIndex)
        step(inter) match {
          case Value(value) => if !inter.isInstanceOf[Value[_, _]] then maybeWrite(Value(value), fileSuffix, stepIndex + 1) // avoid writing twice in case inter is a Value
                               value.unwrap()
          case x            => rec(x, fileSuffix, stepIndex + 1)
        }
      }

      rec(workflow, fileSuffix, 0)
    }
  }
}
