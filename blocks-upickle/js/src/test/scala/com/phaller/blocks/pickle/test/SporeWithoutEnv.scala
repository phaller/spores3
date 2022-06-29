package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.blocks.Builder


@EnableReflectiveInstantiation
object SporeWithoutEnv extends Builder[Int, Int](
  (x: Int) => x + 1
)
