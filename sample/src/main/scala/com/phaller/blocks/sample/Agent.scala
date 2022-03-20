package com.phaller.blocks.sample

import upickle.default.*

import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext

import com.phaller.blocks.{Block, Builder, BlockData, SerBlockData}
import com.phaller.blocks.Block.env
import com.phaller.blocks.pickle.given


object AppendThree extends Builder[List[String], List[String]](
  (strings: List[String]) => strings ::: List("three")
)

object AppendString extends Block.Builder[String, List[String], List[String]](
  (strings: List[String]) => strings ::: List(env)
)

object AppendInt extends Block.Builder[Int, List[String], List[String]](
  (strings: List[String]) => strings ::: List("" + env)
)

object AgentMain {

  def main(args: Array[String]): Unit = {
    // initial state of the agent
    val lst = List("one", "two")

    // create agent and initialize with list
    val agent = Agent(lst)

    // send serializable block to agent
    val data = BlockData(AppendThree, None)
    agent.sendOff(data)

    val resFut = agent.getAsync()
    val d = Duration(10, "sec")
    val res = Await.result(resFut, d)
    println(res)

    // send block that appends its environment, a string
    val example = "four"
    val appendString = BlockData(AppendString, Some(example))
    agent.sendOff(appendString)

    val res2 = Await.result(agent.getAsync(), d)
    println(res2)

    // send block that appends its environment, an integer
    val appendInt = BlockData(AppendInt, Some(5))
    agent.sendOff(appendInt)

    val res3 = Await.result(agent.getAsync(), d)
    println(res3)
  }

}


sealed trait Message[T]
case class Get[T](p: Promise[T]) extends Message[T]
case class ApplyBlock[T](serialized: String) extends Message[T]


/**
  * An agent maintaining a value of type `T` which must be
  * serializable using a `ReadWriter[T]`.
  */
class Agent[T : ReadWriter] (init: T) { self =>
  import java.util.concurrent.ConcurrentLinkedQueue
  import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

  private val mailbox = new ConcurrentLinkedQueue[Message[T]]()
  private val idle = new AtomicBoolean(true)

  private val state: AtomicReference[T] = new AtomicReference(init)

  def sendOff[E](blockData: BlockData[E])(using ReadWriter[BlockData[E]]): Unit = {
    // serialize
    val pickledData = write(blockData)

    enqueueSerialized(pickledData)
  }

  class Task extends Runnable {
    def run(): Unit = {
      if (!idle.get()) {
        try {
          mailbox.poll() match {
            case ApplyBlock(serialized) =>
              // deserialize
              val unpickledData = read[SerBlockData](serialized)
              val unpickledBlock = unpickledData.toBlock[T, T]

              // update state by applying the unpickled block
              val oldState = state.get()
              val newState = unpickledBlock(oldState)
              state.set(newState)

            case Get(promise) =>
              promise.success(state.get())

            case null => // do nothing
          }
        } finally {
          idle.set(true)
          self.checkMailbox()
        }
      } else {
        assert(false, "attempting to run task when idle == true")
      }
    }
  }

  private def checkMailbox()(using ctx: ExecutionContext): Unit =
    // only one concurrent caller succeeds in setting `idle` to false *and* seeing `idle` == true
    if (!mailbox.isEmpty() && idle.getAndSet(false))
      try {
        ctx.execute(new Task())
      } catch {
        case e: RuntimeException =>
          idle.set(true)
          throw e
      }

  private def enqueueSerialized(serialized: String): Unit = {
    mailbox.offer(ApplyBlock(serialized))
    checkMailbox()
  }

  def getAsync(): Future[T] = {
    val p = Promise[T]()
    mailbox.offer(Get(p))
    checkMailbox()
    p.future
  }

}
