package com.phaller.blocks

import scala.scalajs.js.annotation._
import scala.scalajs.reflect.Reflect

@JSExportTopLevel("Creator")
object Creator {

  @JSExport("apply")
  def apply[E, T, R](name: String): Block.Builder[E, T, R] = {
    val creatorClassOpt = Reflect.lookupLoadableModuleClass(name + "$")
    if (creatorClassOpt.nonEmpty)
      creatorClassOpt.get.loadModule().asInstanceOf[Block.Builder[E, T, R]]
    else
      null
  }

  @JSExport("applyNoEnv")
  def applyNoEnv[T, R](name: String): Builder[T, R] = {
    val creatorClassOpt = Reflect.lookupLoadableModuleClass(name + "$")
    if (creatorClassOpt.nonEmpty)
      creatorClassOpt.get.loadModule().asInstanceOf[Builder[T, R]]
    else
      null
  }

  @JSExport("serbuilder")
  def serbuilder[T, R](name: String): SerBuilder[T, R] = {
    val creatorClassOpt = Reflect.lookupLoadableModuleClass(name + "$")
    if (creatorClassOpt.nonEmpty)
      creatorClassOpt.get.loadModule().asInstanceOf[SerBuilder[T, R]]
    else
      null
  }

}
