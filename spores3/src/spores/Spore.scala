package spores

import upickle.default.ReadWriter


/** A serializable closure of type `T`. Guaranteed to not cause runtime errors
  * when created, serialized, deserialized, and unwrapped.
  *
  * Use [[get]] to extract the packed closure.
  *
  * Use [[withEnv]] to partially apply the closure of type `T1 => R` to a value
  * of type `T1`. Use [[withEnv2]] to apply it to a value of type `Spore[T1]`.
  *
  * Use [[withCtx]] to partially apply the closure of type `T1 ?=> R` to a value
  * of type `T1`. Use [[withCtx2]] to apply it to a value of type `Spore[T1]`.
  *
  * Spores are created by:
  *   - (JVM) The [[Spore]] lambda factories: `apply`. Requires explicit capture
  *     of environment variables.
  *   - (JVM) The [[Spore]] lambda factory: `auto`. Implicitly captures
  *     environment variables.
  *   - (JVM, Native, ScalaJS) Packing a top-level object which extends the
  *     [[SporeBuilder]] trait.
  *   - (JVM, Native, ScalaJS) Packing a top-level class which extends the
  *     [[SporeClassBuilder]] trait.
  *   - (JVM, Native, ScalaJS) The [[Env]] factory for packing a value of type
  *     `T` for which there is a Spore[ReadWriter[T]].
  *
  * Serializing and deserializing a Spore is easiest done by using the `upickle`
  * library.
  *
  * Compile-time macros guarantee that it is safe to create, serialize,
  * deserialize, and unwrap the packed closure. Creating a Spore is guaranteed
  * to not cause runtime errors. Serializing, deserializing, and unwrapping a
  * Spore is guaranteed to not cause runtime errors.
  *
  * @example
  *   {{{
  * val mySpore: Spore[Int => String] = Spore.apply { x => x.toString.reverse }
  * val myAppliedSpore: Spore[String] = mySpore.withEnv(10)
  * val serialized = upickle.default.write(myAppliedSpore)
  * val deserialized = upickle.default.read[Spore[String]](serialized)
  * val result = deserialized.get()
  * result // "01"
  *   }}}
  *
  * @tparam T
  *   The type of the packed closure.
  */
type Spore[+T] = Spore0[ReadWriter, T]


/** A factory for creating Spores that are safe to serialize and deserialize.
  *
  * Note: The Spore factory methods only work on the JVM. Use the
  * [[spores.SporeBuilder]] or [[spores.SporeClassBuilder]] for ScalaJS and
  * ScalaNative.
  */
object Spore extends SporeObjectCompanion {

  /** Pack a value of type `T` as a `Spore[T]` using an implicit
    * `Spore[ReadWriter[T]]` instance.
    *
    * @param v
    *   The value to pack.
    * @param ev
    *   The implicit `Spore[ReadWriter[T]]` used for packing the value.
    * @tparam T
    *   The type of the value to pack.
    * @return
    *   A new `Spore[T]` with the packed value.
    */
  def value[T](v: T)(using ev: Spore[ReadWriter[T]]): Spore[T] = {
    Spore0.value(v)
  }
}
