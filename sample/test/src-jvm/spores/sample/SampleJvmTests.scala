package spores.sample

import utest.*


object SampleJvmTests extends TestSuite {

  val tests = Tests {
    test("testSporeExample") {
      SporeExample.main(Array())
    }

    test("testAutoCaptureExample") {
      AutoCaptureExample.main(Array())
    }

    test("testAgentMain") {
      AgentMain.main(Array())
    }

    test("testFutures") {
      Futures.main(Array())
    }

    test("testFutureMap") {
      FutureMap.main(Array())
    }

    test("testParallelTreeReduction") {
      ParallelTreeReduction.main(Array())
    }

    test("testWorkflowMain") {
      WorkflowMain.main(Array())
    }

    test("testForComprehension") {
      ForComprehension.main(Array())
    }
  }
}
