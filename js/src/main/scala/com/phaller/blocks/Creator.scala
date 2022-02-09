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
  def applyNoEnv[E, T, R](name: String): Block.BuilderNoEnv[E, T, R] = {
    val creatorClassOpt = Reflect.lookupLoadableModuleClass(name + "$")
    if (creatorClassOpt.nonEmpty)
      creatorClassOpt.get.loadModule().asInstanceOf[Block.BuilderNoEnv[E, T, R]]
    else
      null
  }

}
