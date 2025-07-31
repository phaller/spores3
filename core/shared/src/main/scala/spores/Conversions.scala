package spores

/** A collection of conversions. Contains conversions from `Spore[Function1[T1,
  * R]]` to `Function1[T1, R]` etc..
  */
object Conversions {

  given conversionSporeToFunction0[R]: Conversion[Spore[() => R], (() => R)] = { spore => spore.unwrap() }
  given conversionSporeToFunction1[T1, R]: Conversion[Spore[T1 => R], (T1 => R)] = { spore => spore.unwrap() }
  given conversionSporeToFunction2[T1, T2, R]: Conversion[Spore[(T1, T2) => R], ((T1, T2) => R)] = { spore => spore.unwrap() }
  given conversionSporeToFunction3[T1, T2, T3, R]: Conversion[Spore[(T1, T2, T3) => R], ((T1, T2, T3) => R)] = { spore => spore.unwrap() }
  given conversionSporeToFunction4[T1, T2, T3, T4, R]: Conversion[Spore[(T1, T2, T3, T4) => R], ((T1, T2, T3, T4) => R)] = { spore => spore.unwrap() }
  given conversionSporeToFunction5[T1, T2, T3, T4, T5, R]: Conversion[Spore[(T1, T2, T3, T4, T5) => R], ((T1, T2, T3, T4, T5) => R)] = { spore => spore.unwrap() }
  given conversionSporeToFunction6[T1, T2, T3, T4, T5, T6, R]: Conversion[Spore[(T1, T2, T3, T4, T5, T6) => R], ((T1, T2, T3, T4, T5, T6) => R)] = { spore => spore.unwrap() }
  given conversionSporeToFunction7[T1, T2, T3, T4, T5, T6, T7, R]: Conversion[Spore[(T1, T2, T3, T4, T5, T6, T7) => R], ((T1, T2, T3, T4, T5, T6, T7) => R)] = { spore => spore.unwrap() }

  // Conversions for context functions are not supported:
  // > Implementation restriction: cannot convert this expression to
  // > `Conversion[spores.Spore[(T1) ?=> R], (T1) ?=> R]` because its result
  // > type `(T1) ?=> R` is a contextual function type.

}
