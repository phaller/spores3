package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.blocks.Block


@EnableReflectiveInstantiation
object MyBlock extends Block.Builder[Int, Int, Int](
  env => x => env + x + 1
)
