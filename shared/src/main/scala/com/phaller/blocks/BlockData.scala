package com.phaller.blocks

import scala.quoted.*


object BlockData {

  inline def apply[N, T, R](inline builder: TypedBuilder[N, T, R], inline envOpt: Option[N]): BlockData[T, R] { type Env = N } = ${ applyCode('builder, 'envOpt) }

  def applyCode[N, T, R](builderExpr: Expr[TypedBuilder[N, T, R]], envOptExpr: Expr[Option[N]])(using Type[N], Type[T], Type[R], Quotes): Expr[BlockData[T, R] { type Env = N }] = {
    import quotes.reflect.*

    def allOwnersOK(owner: Symbol): Boolean =
      owner.isNoSymbol || ((owner.flags.is(Flags.Module) || owner.flags.is(Flags.Package)) && allOwnersOK(owner.owner))

    val tree: Term = builderExpr.asTerm
    val builderTpe = tree.tpe
    val owner = builderTpe.typeSymbol.maybeOwner
    if (!allOwnersOK(owner)) {
      report.error("An owner of the provided builder is neither an object nor a package.")
    }

    val fn = Expr(tree.show)
    '{ new BlockData[T, R]($fn) {
      type Env = N
      def envOpt = $envOptExpr
    } }
  }

}

abstract class BlockData[T, R](val fqn: String) { self =>

  type Env

  def envOpt: Option[Env]

  def toBlock: Block[T, R] { type Env = self.Env } = {
    if (envOpt.isEmpty) {
      val builder = Creator.applyNoEnv[T, R](fqn)
      builder[Env]()
    } else {
      val builder = Creator[Env, T, R](fqn)
      builder(envOpt.get)
    }
  }

}

case class PackedBlockData(fqn: String, envOpt: Option[String] = None) {

  def toBlock[T, R]: Block[T, R] = {
    val builder = Creator.packedBuilder[T, R](fqn)
    builder.createBlock(envOpt)
  }

}
