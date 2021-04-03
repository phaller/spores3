package com.phaller.blocks.pickle.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.phaller.blocks.Block
import com.phaller.blocks.Creator

import upickle.default._


@RunWith(classOf[JUnit4])
class SharedPickleTests {

  @Test
  def test1(): Unit = {
    val res = write(Seq(1, 2, 3))
    assert(res == "[1,2,3]")
  }

}
