package com.phaller.blocks


case class BlockData[E](fqn: String, envOpt: Option[E] = None) {

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
