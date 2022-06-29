package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.blocks.Spore


@EnableReflectiveInstantiation
object MySpore extends Spore.Builder[Int, Int, Int](
  env => x => env + x + 1
)
