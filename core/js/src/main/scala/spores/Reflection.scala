package spores


private[spores] object Reflection {

  import scala.scalajs.reflect.Reflect

  export scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

  def loadModule(name: String): Any = {
    val creatorClassOpt = Reflect.lookupLoadableModuleClass(name)
    if (creatorClassOpt.nonEmpty)
      creatorClassOpt.get.loadModule()
    else
      throw new Exception(s"Module class $name not found")
  }
}
