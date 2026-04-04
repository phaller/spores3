package spores

import utest._

import spores.default.given
import spores.default.*
import spores.TestUtils.*

object SporeBuilderErrorTestsDefs {
  class NotObjObj extends SporeBuilder[Int => Int](x => x)

  class SomeClass:
    object NotTopLevel extends SporeBuilder[Int => Int](x => x)

  def someMethod: SporeBuilder[Int => Int] = {
    object NotTopLevel extends SporeBuilder[Int => Int](x => x)
    NotTopLevel
  }
}

object SporeBuilderErrorTests extends TestSuite {
  import SporeBuilderErrorTestsDefs.*

  val tests = Tests {
    test("testClassSporeBuilderError") {
      assert:
        typeCheckErrorMessages:
          """
          new NotObjObj().build()
          """
        .contains:
          """
          The provided SporeBuilder `spores.SporeBuilderErrorTestsDefs$.NotObjObj` is not an object.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          val notObjObj = new NotObjObj()
          notObjObj.build()
          """
        .contains:
          """
          The provided SporeBuilder `spores.SporeBuilderErrorTestsDefs$.NotObjObj` is not an object.
          """.trim()
    }

    test("testNotTopLevelError") {
      assert:
        typeCheckErrorMessages:
          """
          val notTopLevel = new SomeClass().NotTopLevel
          notTopLevel.build()
          """
        .contains:
          """
          The provided SporeBuilder `spores.SporeBuilderErrorTestsDefs$.SomeClass.NotTopLevel$` is not a top-level object; its owner `SomeClass` is not a top-level object nor a package.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          val notObject = someMethod
          notObject.build()
          """
        .contains:
          """
          The provided SporeBuilder `spores.SporeBuilder` is not an object.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          object Builder extends SporeBuilder[Int => String](x => x.toString.reverse)
          Builder.build()
          """
        .exists:
          _.matches:
            raw"""
            The provided SporeBuilder `.*Builder\$$` is not a top-level object; its owner `.*` is not a top-level object nor a package.
            """.trim()
    }
  }
}
