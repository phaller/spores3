package spores.sample

import utest.*


object SampleTests extends TestSuite {

  val tests = Tests {
    test("testBuilderExample") {
      BuilderExample.main(Array())
    }
  }
}
