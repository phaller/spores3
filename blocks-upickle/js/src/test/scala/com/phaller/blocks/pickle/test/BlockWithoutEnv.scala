package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.blocks.Builder


@EnableReflectiveInstantiation
object BlockWithoutEnv extends Builder[Int, Int](
  (x: Int) => x + 1
)
