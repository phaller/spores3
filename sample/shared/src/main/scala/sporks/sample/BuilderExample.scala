package spores.sample

import spores.*
import spores.given
import spores.sample.platform.*


object BuilderExample {

  object Spore1 extends SporeBuilder[Int => String](x => x.toString.reverse)

  object Spore2 extends SporeBuilder[Int => Int => String](env => x => (env + x).toString.reverse)

  object Predicate extends SporeBuilder[Int => Boolean](x => x > 10)

  object HigherLevelFilter extends SporeBuilder[Spore[Int => Boolean] => Int => Boolean](env => x => env.unwrap().apply(x))

  object SporeOption extends SporeBuilder[Option[Int] => Int => String](env => x => env.map(_ + x).map(_.toString.reverse).getOrElse(""))

  object SporeWithCtx extends SporeBuilder[Int ?=> String](summon[Int].toString().reverse)

  class Constant[T] extends SporeClassBuilder[T => T](x => x)


  def main(args: Array[String]): Unit = {
    // `build` the `SporeBuilder` to get a `Spore` and `unwrap` it to
    // reveal the packed function.
    println(
      Spore1.build().unwrap()(10)
    )

    // `withEnv` to pack an environment into the `Spore`.
    println(
      Spore2.build().withEnv(11).unwrap()(10)
    )

    // The resulting `Spore` is a simple data structure.
    println(
      Spore2.build().withEnv(11)
    )

    // Higher order spores can pack other spores in their environment.
    println(
      HigherLevelFilter.build().withEnv(Predicate.build()).unwrap()(11)
    )

    // Besides primitive types, standard library types like `Option` can also be
    // packed in the environment.
    println(
      SporeOption.build().withEnv(Some(10)).unwrap()(13)
    )

    // The environment paramter can also be a context parameter. A context
    // parameter can be packed using the `withCtx` method.
    println(
      SporeWithCtx.build().withCtx(99).unwrap()
    )

    // The `SporeClassBuilder` can be used to create spores with type
    // parameters.
    println({
      val constant10 = new Constant[Int]().build().withEnv(10)
      constant10.unwrap()
    })

    // ... Although, try to stick to the `SporeBuilder` for performance
    // reasons. Their main use case can be found in package.scala.

    // Another method for creating spores is by using the `Spore.apply`
    // method. This can be imported from the `spores.jvm` package, as it is
    // only available on the JVM. It is a convenient method for creating
    // spore lambdas. See the example in LambdaExample.scala.
    //
    // val lambda = Spore.apply[Int => String] { x => x.toString.reverse }

    // A `Spore` can be serialized/pickled to JSON by using `upickle`.
    writeToFile(Spore1.build(), "Spore1.json")
    println(
      readFromFile[Spore[Int => String]]("Spore1.json")
    )

    writeToFile(Spore2.build().withEnv(10), "Spore2.json")
    println(
      readFromFile[Spore[Int => String]]("Spore2.json")
    )

    writeToFile(HigherLevelFilter.build().withEnv(Predicate.build()), "Filter.json")
    println(
      readFromFile[Spore[Int => Boolean]]("Filter.json")
    )
    println(
      readFromFile[Spore[Int => Boolean]]("Filter.json").unwrap().apply(12)
    )
  }
}
