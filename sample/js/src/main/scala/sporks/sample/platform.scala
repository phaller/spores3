package spores.sample
package platform

import upickle.default.*
import scalajs.js.Dynamic.global


private def readFileSync(path: String, encoding: String): String =
  val fs = global.require("fs")
  fs.readFileSync(path, encoding).asInstanceOf[String]

private def writeFileSync[T: ReadWriter](file: String, data: String, encoding: String): Unit =
  val fs = global.require("fs")
  fs.writeFileSync(file, data, encoding)

inline def readFromFile[T: ReadWriter](fname: String): T =
  val json = readFileSync(fname, "utf8")
  read[T](json)

inline def writeToFile[T: ReadWriter](inline packed: T, fname: String): Unit =
  val data = write(packed)
  writeFileSync(fname, data, "utf8")
