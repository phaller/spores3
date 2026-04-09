package spores

import utest._

import spores.default.given
import spores.default.*
import spores.TestUtils.*

object SporeClassBuilderErrorTestsDefs {
  object NotClzClz extends SporeClassBuilder[Int => Int] {
    override def body = x => x
  }

  def someMethod: SporeClassBuilder[Int => Int] = {
    class Local extends SporeClassBuilder[Int => Int] {
      override def body = x => x
    }
    new Local()
  }

  class NestedBuilderInClass:
    class Inner extends SporeClassBuilder[Int] {
      override def body = 10
    }

  class ClassWithoutPublicConstructor private () extends SporeClassBuilder[Int => Int] {
    override def body = x => x
  }
  object ClassWithoutPublicConstructor:
    def apply(): ClassWithoutPublicConstructor = new ClassWithoutPublicConstructor()

  class ClassWithParameters(i: Int) extends SporeClassBuilder[() => Int] {
    override def body = () => i
  }

  class F[T]
  class ClassWithContext1[T: F] extends SporeClassBuilder[F[T]] {
    override def body = summon
  }
  class ClassWithContext2[T](using F[T]) extends SporeClassBuilder[F[T]] {
    override def body = summon
  }
  class ClassWithContext3[T](implicit f: F[T]) extends SporeClassBuilder[F[T]] {
    override def body = summon
  }
}

object SporeClassBuilderErrorTests extends TestSuite {
  import SporeClassBuilderErrorTestsDefs.*

  val tests = Tests {
    test("testObjectSporeClassBuilderError") {
      assert:
        typeCheckErrorMessages:
          """
          NotClzClz.build()
          """
        .contains:
          """
          The provided SporeClassBuilder `spores.SporeClassBuilderErrorTestsDefs$.NotClzClz$` is not a class.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          val notClzClz = NotClzClz
          notClzClz.build()
          """
        .contains:
          """
          The provided SporeClassBuilder `spores.SporeClassBuilderErrorTestsDefs$.NotClzClz$` is not a class.
          """.trim()
    }

    test("testSporeClassBuilderNestedInClassError") {
      assert:
        typeCheckErrorMessages:
          """
          val builder = new NestedBuilderInClass()
          val pred = new builder.Inner()
          pred.build()
          """
        .contains:
          """
          The provided SporeClassBuilder `spores.SporeClassBuilderErrorTestsDefs$.NestedBuilderInClass.Inner` is nested in a class.
          """.trim()
    }

    test("testSporeClassBuilderNestedInMethodError") {
      assert:
        typeCheckErrorMessages:
          """
          someMethod.build()
          """
        .contains:
          """
          The provided SporeClassBuilder `spores.SporeClassBuilder` is not a concrete class.
          """.trim()
    }

    test("testSporeClassBuilderWithPrivateConstructorError") {
      assert:
        typeCheckErrorMessages:
          """
          ClassWithoutPublicConstructor().build()
          """
        .contains:
          """
          The provided SporeClassBuilder `spores.SporeClassBuilderErrorTestsDefs$.ClassWithoutPublicConstructor` `<init>` does not have a public constructor.
          """.trim()
    }

    test("testSporeClassBuilderWithParameterError") {
      assert:
        typeCheckErrorMessages:
          """
          ClassWithParameters(10).build()
          """
        .contains:
          """
          The constructor of the provided SporeClassBuilder `spores.SporeClassBuilderErrorTestsDefs$.ClassWithParameters` `<init>` does not have an empty parameter list.
          """.trim()
    }

    test("testSporeClassBuilderWithContextParameterError") {
      // Catches a common mistake in which implicit parameters are used in the
      // constructor. For example, this would seem like a reasonable thing to do,
      // but will not work:
      //
      // class PackedRW[T: ReadWriter] extends SporeClassBuilder[ReadWriter[T]](summon[ReadWriter[T]])
      // given Spore[ReadWriter[T]] = PackedRW[T].build()
      //
      // // This will crash at runtime, as the init method is assumed to not have any params.
      // summon[Spore[ReadWriter[Int]]].get()

      assert:
        typeCheckErrorMessages:
          """
          given F[Int] = new F[Int]()
          ClassWithContext1[Int].build()
          """
        .contains:
          """
          The constructor of the provided SporeClassBuilder `spores.SporeClassBuilderErrorTestsDefs$.ClassWithContext1` `<init>` contains a context parameter list.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          given F[Int] = new F[Int]()
          ClassWithContext2[Int].build()
          """
        .contains:
          """
          The constructor of the provided SporeClassBuilder `spores.SporeClassBuilderErrorTestsDefs$.ClassWithContext2` `<init>` contains a context parameter list.
          """.trim()

      assert:
        typeCheckErrorMessages:
          """
          given F[Int] = new F[Int]()
          ClassWithContext3[Int].build()
          """
        .contains:
          """
          The constructor of the provided SporeClassBuilder `spores.SporeClassBuilderErrorTestsDefs$.ClassWithContext3` `<init>` contains a context parameter list.
          """.trim()
    }
  }
}
