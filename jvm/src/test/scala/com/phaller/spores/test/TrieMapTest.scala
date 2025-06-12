package spores.test

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import scala.collection.concurrent.TrieMap

import spores.Spore


case class Customer(name: String, customerNo: Int)
case class CustomerInfo(customerNo: Int, age: Int, since: Int)

@RunWith(classOf[JUnit4])
class TrieMapTest {

  type CustomerMap = TrieMap[Int, CustomerInfo]

  val customerData = TrieMap.empty[Int, CustomerInfo]

  @Test
  def test(): Unit = {
    val s = Spore[CustomerMap, List[Customer], Float](customerData) { data => cs =>
      val infos = cs.flatMap { c =>
        data.get(c.customerNo) match {
          case Some(info) => List(info)
          case None => List()
        }
      }
      val sumAges = infos.foldLeft(0)(_ + _.age).toFloat
      if (infos.size == 0) 0
      else sumAges / infos.size
    }

    val res = s(List())
    assert(res == 0)
  }

}
