package com.phaller.blocks

object Creator {

  def apply[E, T, R](name: String): Block.Builder[E, T, R] = {
    val creatorClass = Class.forName(name + "$")
    val creatorField = creatorClass.getDeclaredField("MODULE$")
    creatorField.get(null).asInstanceOf[Block.Builder[E, T, R]]
  }

  def applyNoEnv[T, R](name: String): Builder[T, R] = {
    val creatorClass = Class.forName(name + "$")
    val creatorField = creatorClass.getDeclaredField("MODULE$")
    creatorField.get(null).asInstanceOf[Builder[T, R]]
  }

  def serbuilder[T, R](name: String): SerBuilder[T, R] = {
    val creatorClass = Class.forName(name + "$")
    val creatorField = creatorClass.getDeclaredField("MODULE$")
    creatorField.get(null).asInstanceOf[SerBuilder[T, R]]
  }

}
