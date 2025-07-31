package spores.sample

import spores.*
import spores.given
import spores.sample.platform.*


object LambdaExample {

  val Lambda1 = Spore.apply[Int => String] { x => x.toString.reverse }

  val Lambda2 = Spore.applyWithEnv[Int, Int => String](12) { env => x => (env + x).toString.reverse }

  val Lambda3 = Spore.apply[Option[Int] => Int] { x => x.map { _ + 1 }.getOrElse(0) }

  val Lambda4 = Spore.applyWithCtx[Int, Int](14) { summon[Int] }

  // // Should cause compile error
  // object ShouldFail:
  //   Spore.apply[Int => Int] { x =>
  //     Spore.apply[Int => Int] { y =>
  //       // Invalid capture of variable `x`. Use the first parameter of a spore's body to refer to the spore's environment.
  //       x + y
  //     }.unwrap().apply(x)
  //   }

  // // Should cause compile error
  // import upickle.default.*
  // def SporeFactoryFail[T: ReadWriter] = Spore.apply { summon[ReadWriter[T]] } // Invalid capture of variable `evidence$1`. Use the first parameter of a spore's body to refer to the spore's environment.


  def main(args: Array[String]): Unit = {
    assert(
      Lambda1.unwrap().apply(10) == "01"
    )
    println(
      Lambda1.unwrap().apply(10)
    )

    assert(
      Lambda1.withEnv(100).unwrap() == "001"
    )
    println(
      Lambda1.withEnv(100).unwrap()
    )

    assert(
      Lambda2.withEnv(10).unwrap() == "22"
    )
    println(
      Lambda2.withEnv(10).unwrap()
    )

    assert(
      Lambda3.unwrap().apply(Some(10)) == 11
    )
    println(
      Lambda3.unwrap().apply(Some(10))
    )

    assert(
      Lambda4.unwrap() == 14
    )
    println(
      Lambda4.unwrap()
    )

    writeToFile(Lambda1, "Lambda1.json")
    assert(
      readFromFile[Spore[Int => String]]("Lambda1.json").unwrap().apply(10) == "01"
    )
    println(
      readFromFile[Spore[Int => String]]("Lambda1.json")
    )

    writeToFile(Lambda2, "Lambda2.json")
    assert(
      readFromFile[Spore[Int => String]]("Lambda2.json").withEnv(10).unwrap() == "22"
    )
    println(
      readFromFile[Spore[Int => String]]("Lambda2.json")
    )

    writeToFile(Lambda3, "Lambda3.json")
    assert(
      readFromFile[Spore[Option[Int] => Int]]("Lambda3.json").unwrap().apply(Some(10)) == 11
    )
    println(
      readFromFile[Spore[Option[Int] => Int]]("Lambda3.json")
    )

    writeToFile(Lambda3.withEnv(Some(42)), "Lambda3WithEnv.json")
    assert(
      readFromFile[Spore[Int]]("Lambda3WithEnv.json").unwrap() == 43
    )
    println(
      readFromFile[Spore[Int]]("Lambda3WithEnv.json")
    )

  }
}
