package spores


private[spores] object Reflection {

  import scala.scalanative.reflect.Reflect

  export scala.scalanative.reflect.annotation.EnableReflectiveInstantiation

  def loadModuleFieldValue[T](name: String): T = {
    Reflect.lookupLoadableModuleClass(name) match {
      case Some(clazz) => {
        clazz.loadModule().asInstanceOf[T]
      }
      case None => {
        throw new Exception(s"Module class $name not found")
      }
    }
  }

  def loadClassInstance[T](name: String): T = {
    Reflect.lookupInstantiatableClass(name) match {
      case Some(clazz) => {
        clazz.newInstance().asInstanceOf[T]
      }
      case None => {
        throw new Exception(s"Class $name not found")
      }
    }
  }
}
