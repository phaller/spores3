package com.phaller.blocks.pickle.test

import scala.scalajs.reflect.Reflect
import scala.scalajs.reflect.annotation.EnableReflectiveInstantiation

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import com.phaller.blocks.{Block, Creator}

import upickle.default._


@EnableReflectiveInstantiation
object A {
}

@EnableReflectiveInstantiation
object MyBlock2 extends Block.Creator[Int, Int, Int](
  (x: Int) => Block.env + x + 1
)

@RunWith(classOf[JUnit4])
class PickleTests {

  @Test
  def testReflect(): Unit = {
    val modClass1 = Reflect.lookupLoadableModuleClass("A$")
    val modClass2 = Reflect.lookupLoadableModuleClass("com.phaller.blocks.pickle.test.A$")
    val loaded = modClass2.get.loadModule()
    assert(modClass1.isEmpty)
    assert(modClass2.nonEmpty)
    assert(loaded != null)

    val modClass3 = Reflect.lookupLoadableModuleClass("com.phaller.blocks.pickle.test.MyBlock2$")
    assert(modClass3.nonEmpty)
    val loadedMyBlock2 = modClass3.get.loadModule().asInstanceOf[Block.Creator[Int, Int, Int]]
    assert(loadedMyBlock2 != null)

    val s = loadedMyBlock2(12)
    val res = s(3)
    assert(res == 16)
  }

  @Test
  def testCreator(): Unit = {
    val c = Creator[Int, Int, Int]("com.phaller.blocks.pickle.test.MyBlock2")
    val s = c(12)
    val res = s(3)
    assert(res == 16)
  }

  @Test
  def testUpickle(): Unit = {
    val res = write(Seq(1, 2, 3))
    assert(res == "[1,2,3]")
  }

}
