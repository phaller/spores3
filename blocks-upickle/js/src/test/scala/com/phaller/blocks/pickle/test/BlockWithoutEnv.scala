package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.blocks.Block


@EnableReflectiveInstantiation
object BlockWithoutEnv extends Block.BuilderNoEnv[Nothing, Int, Int](
  (x: Int) => x + 1
)
