package spores


private[spores] object Reflection {

  import scala.annotation.StaticAnnotation
  // Dummy annotation as JVM reflection is enabled by default
  class EnableReflectiveInstantiation extends StaticAnnotation

  def loadModule(name: String): Any = {
    Class.forName(name).getDeclaredField("MODULE$").get(null)
  }
}
