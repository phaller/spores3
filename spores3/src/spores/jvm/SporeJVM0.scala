package spores.jvm

import spores.*


/** Internal API. */
private[spores] object SporeJVM0 {

  // The Spore factory only works on the JVM. The generated class here is not a
  // top-level class. For this reason, it cannot be reflectively instantiated on
  // ScalaJS or ScalaNative. For more information, see:
  // https://github.com/portable-scala/portable-scala-reflect.

  inline def apply[F[_], T](inline body: T): Spore0[F, T] = {
    ${ applyMacro('body) }
  }

  inline def applyWithEnv[F[_], E, T](inline env: E)(inline body: E => T)(using ev: Spore0[F, F[E]]): Spore0[F, T] = {
    apply[F, E => T](body).withEnv(env)(using ev)
  }

  inline def applyWithCtx[F[_], E, T](inline env: E)(inline body: E ?=> T)(using ev: Spore0[F, F[E]]): Spore0[F, T] = {
    apply[F, E ?=> T](body).withCtx(env)(using ev)
  }

  import scala.quoted.*

  private def applyMacro[F[_], T](bodyExpr: Expr[T])(using Type[F], Type[T], Quotes): Expr[Spore0[F, T]] = {
    Macros.checkBodyExpr(bodyExpr)
    '{
      class Lambda extends SporeLambdaBuilder0[F, T] {
        override def body: T = { $bodyExpr }
      }
      (new Lambda()).build()
    }
  }

  inline def auto[F[_], T](inline body: T): Spore0[F, T] = {
    ${ autoMacro('body) }
  }

  import scala.quoted.*

  private def autoMacro[F[_], T](bodyExpr: Expr[T])(using Type[F], Type[T], Quotes): Expr[Spore0[F, T]] = {
    import quotes.reflect.*

    // 1. Find all captured symbols
    val foundIds = Macros.findCapturedIds(bodyExpr.asTerm)

    // 2. Find all evidence for each captured symbol
    // Throws an error if any captured symbol [E] is missing an evidence F[E] in
    // implicit scope 
    val evidence: List[(Tree, Tree)] = Macros.findEvidence[F](foundIds)

    // Group by the captured identifier's full name
    val captures: List[List[(Tree, Tree)]] = evidence.groupBy(_._1.symbol.fullName).toList.sortBy(_._1).map(_._2)

    // 3. Lift captured variables into parameters
    val liftedExpr: Term = Macros.liftAllSymbolGroups(Symbol.spliceOwner, captures.map(_.map(_._1)).reverse, bodyExpr.asTerm)

    // 4. Construct Spore0 from lifted body
    val packed: Expr[Spore0[F, Any]] = '{
      val lambda = {
        // TODO: Use type of liftedExpr instead of `Any` here.
        class Lambda extends SporeLambdaBuilder0[F, Any] {
          override def body = { ${liftedExpr.asExpr} }
        }
        (new Lambda())
      }
      lambda.build()
    }

    // 5. Apply captured environment variables
    var tmp = packed
    for capevi <- captures do {
      val cap = capevi.head._1.symbol
      val evi = capevi.head._2.asExpr
      val env: Expr[Any] = Ref(cap).asExpr
      tmp = '{
        $tmp
          .asInstanceOf[Spore0[F, Any => Any]]
          .withEnv($env)(using $evi.asInstanceOf)
      }
    }

    '{
      $tmp.asInstanceOf[Spore0[F, T]]
    }
  }
}
