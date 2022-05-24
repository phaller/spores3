package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.blocks.Block
import com.phaller.blocks.Block.{env, checked}


@EnableReflectiveInstantiation
object MyBlock extends Block.Builder[Int, Int, Int](
  checked((x: Int) => env + x + 1)
)
