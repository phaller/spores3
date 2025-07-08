package spores


private[spores] object Creator {

  private def getModuleFieldValue(name: String) =
    Reflection.loadModule(name + "$")

  def apply[E, T, R](name: String): Spore.Builder[E, T, R] =
    getModuleFieldValue(name).asInstanceOf[Spore.Builder[E, T, R]]

  def applyNoEnv[T, R](name: String): Builder[T, R] =
    getModuleFieldValue(name).asInstanceOf[Builder[T, R]]

  def packedBuilder[T, R](name: String): PackedBuilder[T, R] =
    getModuleFieldValue(name).asInstanceOf[PackedBuilder[T, R]]

}
