package com.phaller.blocks

object Creator {
  def apply[E, T, R](name: String): Block.Builder[E, T, R] = {
    val creatorClass = Class.forName(name + "$")
    val creatorField = creatorClass.getDeclaredField("MODULE$")
    creatorField.get(null).asInstanceOf[Block.Builder[E, T, R]]
  }
}
