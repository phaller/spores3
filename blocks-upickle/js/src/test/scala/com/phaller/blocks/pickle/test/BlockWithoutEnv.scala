package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.blocks.{Builder, checked}


@EnableReflectiveInstantiation
object BlockWithoutEnv extends Builder[Int, Int](
  checked((x: Int) => x + 1)
)
