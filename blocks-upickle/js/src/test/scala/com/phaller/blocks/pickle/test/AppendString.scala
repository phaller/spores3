package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import com.phaller.blocks.Block
import com.phaller.blocks.Block.env


@EnableReflectiveInstantiation
object AppendString extends
    Block.Builder[String, List[String], List[String]](
  (strings: List[String]) => strings ::: List(env))
