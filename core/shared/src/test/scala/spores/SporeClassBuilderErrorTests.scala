package spores

import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Test
import org.junit.Assert.*

import spores.default.given
import spores.default.*
import spores.TestUtils.*

object SporeClassBuilderErrorTests:
  object NotClzClz extends SporeClassBuilder[Int => Int](x => x)

  def someMethod: SporeClassBuilder[Int => Int] = {
    class Local extends SporeClassBuilder[Int => Int](x => x)
    new Local()
  }

  class NestedBuilderInClass:
    class Inner extends SporeClassBuilder[Int](10)

  class ClassWithoutPublicConstructor private () extends SporeClassBuilder[Int => Int](x => x)
  object ClassWithoutPublicConstructor:
    def apply(): ClassWithoutPublicConstructor = new ClassWithoutPublicConstructor()

  class ClassWithParameters(i: Int) extends SporeClassBuilder[() => Int](() => i)

  class F[T]
  class ClassWithContext1[T: F] extends SporeClassBuilder[F[T]](summon)
  class ClassWithContext2[T](using F[T]) extends SporeClassBuilder[F[T]](summon)
  class ClassWithContext3[T](implicit f: F[T]) extends SporeClassBuilder[F[T]](summon)

@RunWith(classOf[JUnit4])
class SporeClassBuilderErrorTests:
  import SporeClassBuilderErrorTests.*

  @Test
  def testObjectSporeClassBuilderError(): Unit =
    assertTrue:
      typeCheckErrorMessages:
        """
        NotClzClz.build()
        """
      .contains:
        """
        The provided SporeClassBuilder `spores.SporeClassBuilderErrorTests$.NotClzClz$` is not a class.
        """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        val notClzClz = NotClzClz
        notClzClz.build()
        """
      .contains:
        """
        The provided SporeClassBuilder `spores.SporeClassBuilderErrorTests$.NotClzClz$` is not a class.
        """.trim()

  @Test
  def testSporeClassBuilderNestedInClassError(): Unit =
    assertTrue:
      typeCheckErrorMessages:
        """
        val builder = new NestedBuilderInClass()
        val pred = new builder.Inner()
        pred.build()
        """
      .contains:
        """
        The provided SporeClassBuilder `spores.SporeClassBuilderErrorTests$.NestedBuilderInClass.Inner` is nested in a class.
        """.trim()

  @Test
  def testSporeClassBuilderNestedInMethodError(): Unit =
    assertTrue:
      typeCheckErrorMessages:
        """
        someMethod.build()
        """
      .contains:
        """
        The provided SporeClassBuilder `spores.SporeClassBuilder` is not a concrete class.
        """.trim()

  @Test
  def testSporeClassBuilderWithPrivateConstructorError(): Unit =
    assertTrue:
      typeCheckErrorMessages:
        """
        ClassWithoutPublicConstructor().build()
        """
      .contains:
        """
        The provided SporeClassBuilder `spores.SporeClassBuilderErrorTests$.ClassWithoutPublicConstructor` `<init>` does not have a public constructor.
        """.trim()

  @Test
  def testSporeClassBuilderWithParameterError(): Unit =
    assertTrue:
      typeCheckErrorMessages:
        """
        ClassWithParameters(10).build()
        """
      .contains:
        """
        The constructor of the provided SporeClassBuilder `spores.SporeClassBuilderErrorTests$.ClassWithParameters` `<init>` does not have an empty parameter list.
        """.trim()

  @Test
  def testSporeClassBuilderWithContextParameterError(): Unit =
    // Catches a common mistake in which implicit parameters are used in the
    // constructor. For example, this would seem like a reasonable thing to do,
    // but will not work:
    //
    // class PackedRW[T: ReadWriter] extends SporeClassBuilder[ReadWriter[T]](summon[ReadWriter[T]])
    // given Spore[ReadWriter[T]] = PackedRW[T].build()
    //
    // // This will crash at runtime, as the init method is assumed to not have any params.
    // summon[Spore[ReadWriter[Int]]].unwrap()

    assertTrue:
      typeCheckErrorMessages:
        """
        given F[Int] = new F[Int]()
        ClassWithContext1[Int].build()
        """
      .contains:
        """
        The constructor of the provided SporeClassBuilder `spores.SporeClassBuilderErrorTests$.ClassWithContext1` `<init>` contains a context parameter list.
        """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        given F[Int] = new F[Int]()
        ClassWithContext2[Int].build()
        """
      .contains:
        """
        The constructor of the provided SporeClassBuilder `spores.SporeClassBuilderErrorTests$.ClassWithContext2` `<init>` contains a context parameter list.
        """.trim()

    assertTrue:
      typeCheckErrorMessages:
        """
        given F[Int] = new F[Int]()
        ClassWithContext3[Int].build()
        """
      .contains:
        """
        The constructor of the provided SporeClassBuilder `spores.SporeClassBuilderErrorTests$.ClassWithContext3` `<init>` contains a context parameter list.
        """.trim()
