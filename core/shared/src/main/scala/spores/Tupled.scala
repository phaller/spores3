package spores

/** Extensions for `tupledN` and `untupledN` on Spores. */
object Tupled {

  private[spores] final class Tupled0[R] extends SporeClassBuilder[Function0[R] => Function1[EmptyTuple, R]]({ fun0 => { case EmptyTuple => fun0() } })
  private[spores] final class Tupled1[T1, R] extends SporeClassBuilder[Function1[T1, R] => Function1[Tuple1[T1], R]]({ fun1 => { case Tuple1(x1) => fun1(x1) } })
  private[spores] final class Tupled2[T1, T2, R] extends SporeClassBuilder[Function2[T1, T2, R] => Function1[Tuple2[T1, T2], R]]({ fun2 => fun2.tupled })
  private[spores] final class Tupled3[T1, T2, T3, R] extends SporeClassBuilder[Function3[T1, T2, T3, R] => Function1[Tuple3[T1, T2, T3], R]]({ fun3 => fun3.tupled })
  private[spores] final class Tupled4[T1, T2, T3, T4, R] extends SporeClassBuilder[Function4[T1, T2, T3, T4, R] => Function1[(T1, T2, T3, T4), R]]({ fun4 => fun4.tupled })
  private[spores] final class Tupled5[T1, T2, T3, T4, T5, R] extends SporeClassBuilder[Function5[T1, T2, T3, T4, T5, R] => Function1[(T1, T2, T3, T4, T5), R]]({ fun5 => fun5.tupled })
  private[spores] final class Tupled6[T1, T2, T3, T4, T5, T6, R] extends SporeClassBuilder[Function6[T1, T2, T3, T4, T5, T6, R] => Function1[(T1, T2, T3, T4, T5, T6), R]]({ fun6 => fun6.tupled })
  private[spores] final class Tupled7[T1, T2, T3, T4, T5, T6, T7, R] extends SporeClassBuilder[Function7[T1, T2, T3, T4, T5, T6, T7, R] => Function1[(T1, T2, T3, T4, T5, T6, T7), R]]({ fun7 => fun7.tupled })

  extension [R](spore: Spore[Function0[R]]) { def tupled0: Spore[Function1[EmptyTuple, R]] = new Tupled0[R]().build().withEnv2(spore) }
  extension [T1, R](spore: Spore[Function1[T1, R]]) { def tupled1: Spore[Function1[Tuple1[T1], R]] = new Tupled1[T1, R]().build().withEnv2(spore) }
  extension [T1, T2, R](spore: Spore[Function2[T1, T2, R]]) { def tupled2: Spore[Function1[Tuple2[T1, T2], R]] = new Tupled2[T1, T2, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, R](spore: Spore[Function3[T1, T2, T3, R]]) { def tupled3: Spore[Function1[Tuple3[T1, T2, T3], R]] = new Tupled3[T1, T2, T3, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, T4, R](spore: Spore[Function4[T1, T2, T3, T4, R]]) { def tupled4: Spore[Function1[(T1, T2, T3, T4), R]] = new Tupled4[T1, T2, T3, T4, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, T4, T5, R](spore: Spore[Function5[T1, T2, T3, T4, T5, R]]) { def tupled5: Spore[Function1[(T1, T2, T3, T4, T5), R]] = new Tupled5[T1, T2, T3, T4, T5, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, T4, T5, T6, R](spore: Spore[Function6[T1, T2, T3, T4, T5, T6, R]]) { def tupled6: Spore[Function1[(T1, T2, T3, T4, T5, T6), R]] = new Tupled6[T1, T2, T3, T4, T5, T6, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, T4, T5, T6, T7, R](spore: Spore[Function7[T1, T2, T3, T4, T5, T6, T7, R]]) { def tupled7: Spore[Function1[(T1, T2, T3, T4, T5, T6, T7), R]] = new Tupled7[T1, T2, T3, T4, T5, T6, T7, R]().build().withEnv2(spore) }

  private[spores] final class Untupled0[R] extends SporeClassBuilder[Function1[EmptyTuple, R] => Function0[R]]({ fun0 => () => fun0(EmptyTuple) })
  private[spores] final class Untupled1[T1, R] extends SporeClassBuilder[Function1[Tuple1[T1], R] => Function1[T1, R]]({ fun1 => t1 => fun1(Tuple1(t1)) })
  private[spores] final class Untupled2[T1, T2, R] extends SporeClassBuilder[Function1[(T1, T2), R] => Function2[T1, T2, R]]({ fun2 => (t1, t2) => fun2((t1, t2)) })
  private[spores] final class Untupled3[T1, T2, T3, R] extends SporeClassBuilder[Function1[(T1, T2, T3), R] => Function3[T1, T2, T3, R]]({ fun3 => (t1, t2, t3) => fun3((t1, t2, t3)) })
  private[spores] final class Untupled4[T1, T2, T3, T4, R] extends SporeClassBuilder[Function1[(T1, T2, T3, T4), R] => Function4[T1, T2, T3, T4, R]]({ fun4 => (t1, t2, t3, t4) => fun4((t1, t2, t3, t4)) })
  private[spores] final class Untupled5[T1, T2, T3, T4, T5, R] extends SporeClassBuilder[Function1[(T1, T2, T3, T4, T5), R] => Function5[T1, T2, T3, T4, T5, R]]({ fun5 => (t1, t2, t3, t4, t5) => fun5((t1, t2, t3, t4, t5)) })
  private[spores] final class Untupled6[T1, T2, T3, T4, T5, T6, R] extends SporeClassBuilder[Function1[(T1, T2, T3, T4, T5, T6), R] => Function6[T1, T2, T3, T4, T5, T6, R]]({ fun6 => (t1, t2, t3, t4, t5, t6) => fun6((t1, t2, t3, t4, t5, t6)) })
  private[spores] final class Untupled7[T1, T2, T3, T4, T5, T6, T7, R] extends SporeClassBuilder[Function1[(T1, T2, T3, T4, T5, T6, T7), R] => Function7[T1, T2, T3, T4, T5, T6, T7, R]]({ fun7 => (t1, t2, t3, t4, t5, t6, t7) => fun7((t1, t2, t3, t4, t5, t6, t7)) })

  extension [R](spore: Spore[Function1[EmptyTuple, R]]) { def untupled0: Spore[Function0[R]] = new Untupled0[R]().build().withEnv2(spore) }
  extension [T1, R](spore: Spore[Function1[Tuple1[T1], R]]) { def untupled1: Spore[Function1[T1, R]] = new Untupled1[T1, R]().build().withEnv2(spore) }
  extension [T1, T2, R](spore: Spore[Function1[(T1, T2), R]]) { def untupled2: Spore[Function2[T1, T2, R]] = new Untupled2[T1, T2, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, R](spore: Spore[Function1[(T1, T2, T3), R]]) { def untupled3: Spore[Function3[T1, T2, T3, R]] = new Untupled3[T1, T2, T3, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, T4, R](spore: Spore[Function1[(T1, T2, T3, T4), R]]) { def untupled4: Spore[Function4[T1, T2, T3, T4, R]] = new Untupled4[T1, T2, T3, T4, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, T4, T5, R](spore: Spore[Function1[(T1, T2, T3, T4, T5), R]]) { def untupled5: Spore[Function5[T1, T2, T3, T4, T5, R]] = new Untupled5[T1, T2, T3, T4, T5, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, T4, T5, T6, R](spore: Spore[Function1[(T1, T2, T3, T4, T5, T6), R]]) { def untupled6: Spore[Function6[T1, T2, T3, T4, T5, T6, R]] = new Untupled6[T1, T2, T3, T4, T5, T6, R]().build().withEnv2(spore) }
  extension [T1, T2, T3, T4, T5, T6, T7, R](spore: Spore[Function1[(T1, T2, T3, T4, T5, T6, T7), R]]) { def untupled7: Spore[Function7[T1, T2, T3, T4, T5, T6, T7, R]] = new Untupled7[T1, T2, T3, T4, T5, T6, T7, R]().build().withEnv2(spore) }
}
