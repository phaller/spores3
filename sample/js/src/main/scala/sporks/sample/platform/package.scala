package spores.sample
package platform

import upickle.default.*
import scalajs.js.Dynamic.global


private lazy val fs = global.require("fs")

def readFromFile[T: ReadWriter](fname: String): T =
  val json = fs.readFileSync(fname, "utf8").asInstanceOf[String]
  read[T](json)

def writeToFile[T: ReadWriter](packed: T, fname: String): Unit =
  val data = write(packed)
  fs.writeFileSync(fname, data, "utf8")
