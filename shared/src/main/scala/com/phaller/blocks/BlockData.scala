package com.phaller.blocks

import scala.quoted.*


object BlockData {

  inline def apply[E](inline builder: TypedBuilder[E, _, _], inline envOpt: Option[E]): BlockData[E] = ${ applyCode('builder, 'envOpt) }

  def applyCode[E](builderExpr: Expr[TypedBuilder[E, _, _]], envOptExpr: Expr[Option[E]])(using Type[E], Quotes): Expr[BlockData[E]] = {
    import quotes.reflect.*
    val tree: Term = builderExpr.asTerm
    // TODO: check that tree is just an identifier referring to an object
    val fn = Expr(tree.show)
    '{ new BlockData[E]($fn, $envOptExpr) }
  }

}

class BlockData[E](val fqn: String, val envOpt: Option[E]) {

  def toBlock[T, R]: Block[T, R] { type Env = E } = {
    if (envOpt.isEmpty) {
      val builder = Creator.applyNoEnv[T, R](fqn)
      builder[E]()
    } else {
      val builder = Creator[E, T, R](fqn)
      builder(envOpt.get)
    }
  }

}

case class SerBlockData(fqn: String, envOpt: Option[String] = None) {

  def toBlock[T, R]: Block[T, R] = {
    val builder = Creator.serbuilder[T, R](fqn)
    builder.createBlock(envOpt)
  }

}
