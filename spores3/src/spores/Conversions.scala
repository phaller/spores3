package spores

/** A collection of conversions. Contains conversions from `Spore0[F, 
  * Function1[T1, R]]` to `Function1[T1, R]` etc..
  */
package object conversions {

  given conversionSporeToFunction0[F[_], R]: Conversion[Spore0[F, () => R], (() => R)] = { spore => spore.get() }
  given conversionSporeToFunction1[F[_], T1, R]: Conversion[Spore0[F, T1 => R], (T1 => R)] = { spore => spore.get() }
  given conversionSporeToFunction2[F[_], T1, T2, R]: Conversion[Spore0[F, (T1, T2) => R], ((T1, T2) => R)] = { spore => spore.get() }
  given conversionSporeToFunction3[F[_], T1, T2, T3, R]: Conversion[Spore0[F, (T1, T2, T3) => R], ((T1, T2, T3) => R)] = { spore => spore.get() }
  given conversionSporeToFunction4[F[_], T1, T2, T3, T4, R]: Conversion[Spore0[F, (T1, T2, T3, T4) => R], ((T1, T2, T3, T4) => R)] = { spore => spore.get() }
  given conversionSporeToFunction5[F[_], T1, T2, T3, T4, T5, R]: Conversion[Spore0[F, (T1, T2, T3, T4, T5) => R], ((T1, T2, T3, T4, T5) => R)] = { spore => spore.get() }
  given conversionSporeToFunction6[F[_], T1, T2, T3, T4, T5, T6, R]: Conversion[Spore0[F, (T1, T2, T3, T4, T5, T6) => R], ((T1, T2, T3, T4, T5, T6) => R)] = { spore => spore.get() }
  given conversionSporeToFunction7[F[_], T1, T2, T3, T4, T5, T6, T7, R]: Conversion[Spore0[F, (T1, T2, T3, T4, T5, T6, T7) => R], ((T1, T2, T3, T4, T5, T6, T7) => R)] = { spore => spore.get() }

}
