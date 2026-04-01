package spores.sample
package platform

import upickle.default.*
import java.nio.file.*
import java.nio.charset.StandardCharsets


def readFromFile[T: ReadWriter](fname: String): T =
  val path = Paths.get(fname)
  val json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8)
  read[T](json)

def writeToFile[T: ReadWriter](packed: T, fname: String): Unit =
  val path = Paths.get(fname)
  val json = write(packed)
  Files.write(path, json.getBytes(StandardCharsets.UTF_8))
