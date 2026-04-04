package spores

import scala.compiletime.testing.{typeChecks, typeCheckErrors}


object TestUtils {

  inline def typeCheckSuccess(inline str: String): Boolean =
    typeChecks(str)

  inline def typeCheckFail(inline str: String): Boolean =
    !typeChecks(str)

  inline def typeCheckErrorMessages(inline str: String): List[String] =
    typeCheckErrors(str).map(_.message)
}
