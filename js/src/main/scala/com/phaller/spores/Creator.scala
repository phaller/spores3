package spores

import scala.scalajs.js.annotation.{JSExportTopLevel, JSExport}
import scala.scalajs.reflect.Reflect


@JSExportTopLevel("Creator")
protected[spores] object Creator {

  private def loadModule(name: String) = {
    val creatorClassOpt = Reflect.lookupLoadableModuleClass(name + "$")
    if (creatorClassOpt.nonEmpty)
      creatorClassOpt.get.loadModule()
    else
      null
  }

  @JSExport("apply")
  def apply[E, T, R](name: String): Spore.Builder[E, T, R] =
    loadModule(name).asInstanceOf[Spore.Builder[E, T, R]]

  @JSExport("applyNoEnv")
  def applyNoEnv[T, R](name: String): Builder[T, R] =
    loadModule(name).asInstanceOf[Builder[T, R]]

  @JSExport("packedBuilder")
  def packedBuilder[T, R](name: String): PackedBuilder[T, R] =
    loadModule(name).asInstanceOf[PackedBuilder[T, R]]

}
