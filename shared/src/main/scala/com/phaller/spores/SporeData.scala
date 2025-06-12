package spores

import scala.quoted.*


/** Enables creating [[SporeData]] objects.
  */
object SporeData {

  /** Creates a `SporeData` instance which serves as a serializable form
    * of a spore. The method requires a *spore builder* which is a
    * top-level object extending either [[Builder]] or
    * [[Spore.Builder]].  Both of these implement the [[TypedBuilder]]
    * trait.
    *
    * @tparam N the type of the spore's environment
    * @tparam T the spore's parameter type
    * @tparam R the spore's result type
    * @param builder the spore builder defining the spore's body
    * @param envOpt the spore's optional environment
    */
  inline def apply[N, T, R](inline builder: TypedBuilder[N, T, R], inline envOpt: Option[N] = None): SporeData[T, R] { type Env = N } = ${ applyCode('builder, 'envOpt) }

  private def applyCode[N, T, R](builderExpr: Expr[TypedBuilder[N, T, R]], envOptExpr: Expr[Option[N]])(using Type[N], Type[T], Type[R], Quotes): Expr[SporeData[T, R] { type Env = N }] = {
    import quotes.reflect.*

    def allOwnersOK(owner: Symbol): Boolean =
      owner.isNoSymbol || ((owner.flags.is(Flags.Module) || owner.flags.is(Flags.Package)) && allOwnersOK(owner.owner))

    val tree       = builderExpr.asTerm
    val builderTpe = tree.tpe
    val owner      = builderTpe.typeSymbol.maybeOwner
    if (!allOwnersOK(owner)) {
      report.error("An owner of the provided builder is neither an object nor a package.")
    }

    val fn = Expr(tree.show)
    '{
      new SporeData[T, R]($fn) {
        type Env = N
        def envOpt = $envOptExpr
      }
    }
  }

}

abstract class SporeData[T, R](val fqn: String) { self =>

  type Env

  def envOpt: Option[Env]

  def toSpore: Spore[T, R] { type Env = self.Env } = {
    if (envOpt.isEmpty) {
      val builder = Creator.applyNoEnv[T, R](fqn)
      builder[Env]()
    } else {
      val builder = Creator[Env, T, R](fqn)
      builder(envOpt.get)
    }
  }

}

case class PackedSporeData(fqn: String, envOpt: Option[String] = None) {

  def toSpore[T, R]: Spore[T, R] = {
    val builder = Creator.packedBuilder[T, R](fqn)
    builder.createSpore(envOpt)
  }

}
