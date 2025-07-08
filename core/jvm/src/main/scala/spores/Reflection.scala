package spores


private[spores] object Reflection {

  import scala.annotation.StaticAnnotation
  // Dummy annotation as JVM reflection is enabled by default
  class EnableReflectiveInstantiation extends StaticAnnotation

  def loadModuleFieldValue[T](name: String): T = {
    Class.forName(name).getDeclaredField("MODULE$").get(null).asInstanceOf[T]
  }

  def loadClassInstance[T](name: String): T = {
    Class.forName(name).getDeclaredConstructor().newInstance().asInstanceOf[T]
  }
}
