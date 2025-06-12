package spores.sample

import scala.util.Random

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

import spores.{Spore, Duplicable}
import spores.Spore.thunk
import spores.Duplicable.duplicate


/**
  * A binary tree ADT where inner tree nodes contain (mutable) integer
  * arrays (for demonstration purposes).
  */
abstract class Tree
case class Branch(left: Tree, data: Array[Int], right: Tree) extends Tree
object Leaf extends Tree

object Tree {
  // for simplicity, all arrays within tree nodes have the same size
  val ARRAY_SIZE = 128

  // how to duplicate a tree
  given Duplicable[Tree] with {
    def duplicate(t: Tree): Tree = t match {
      case Leaf => Leaf
      case Branch(left, arr, right) =>
        val copied = Array.ofDim[Int](ARRAY_SIZE)
        arr.copyToArray(copied)
        Branch(duplicate(left), copied, duplicate(right))
    }
  }
}

/**
  * Implements a parallel tree reduction using futures and blocks.
  *
  * This sample shows how to avoid data races when using mutable data
  * structures by duplicating a block and its accessed data structures
  * before launching the block's concurrent execution.
  *
  * Note: the goal is *not* to show an "optimal" parallel tree
  * reduction algorithm. The implemented algorithm is merely used as
  * an example to show how concurrent code can be made safer by having
  * futures execute duplicated blocks.
  */
object ParallelTreeReduction {

  // create random number generator with given seed value
  private val SEED = 5
  private val rnd = new Random(SEED)

  def main(args: Array[String]): Unit = {
    // generate binary tree with given height
    val tree = generateTree(7)
    // recursive parallel tree reduction
    val resFut = parReduce(tree)
    val res = Await.result(resFut, Duration(60, "sec"))
    println(res)
    assert(res == 806101)
  }

  /**
    * Launches a future that executes the given block.
    *
    * For increased safety, the block (`block`), including its
    * environment, is first duplicated. The created future only uses
    * and executes the duplicated block.
    */
  def safeFuture[R, T <: Spore[Unit, R] : Duplicable](spore: T) = {
    val safeSpore = duplicate(spore)
    Future { safeSpore() }
  }

  /** Implements parallel tree reduction using blocks and futures.
    *
    * Reducing an inner node (`Branch`) is the interesting case.
    *
    * First, zero-argument blocks are created (using `thunk`) which
    * reduce the left and right subtrees. Each block contains the tree
    * that should be reduced in its environment.
    *
    * Then, a safe future is created (using `safeFuture` above) by (a)
    * duplicating the given block, and (b) launching a future that
    * executes the duplicated block.  Concurrently, the array of the
    * current inner node is reduced sequentially (thus, `parReduce`
    * consumes CPU cycles itself).
    *
    * Finally, a future is created (using `flatMap` and `map`) which
    * is resolved when both subtrees have been reduced and the final
    * sum has been computed.
    */
  def parReduce(t: Tree): Future[Int] = t match {
    case Leaf =>
      Future.successful(0)

    case Branch(left, data, right) =>

      val leftBlock = thunk(left) { env =>
        parReduce(env)
      }

      val rightBlock = thunk(right) { env =>
        parReduce(env)
      }

      val leftFut =  safeFuture(leftBlock).flatten
      val rightFut = safeFuture(rightBlock).flatten

      val dataRes = data.reduce(_ + _)

      leftFut.flatMap(leftRes =>
        rightFut.map(rightRes =>
          leftRes + rightRes + dataRes))
  }

  /**
    * Generates binary tree of height `n` with randomly filled arrays.
    */
  def generateTree(n: Int): Tree = {
    if (n == 0) {
      Leaf
    } else {
      // generate Int array
      val numbers = Array.fill(Tree.ARRAY_SIZE) {
        rnd.nextInt(100) // returns random Int from 0..99
      }
      Branch(generateTree(n-1), numbers, generateTree(n-1))
    }
  }

}
