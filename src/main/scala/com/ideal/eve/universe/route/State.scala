package com.ideal.eve.universe.route

import com.ideal.eve.universe.ValueMatcher

import scala.collection.mutable.ListBuffer

/**
  * Created by Christophe on 17/04/2016.
  */
class State[T](val name: String) {
  val next = collection.mutable.Map[ValueMatcher, ListBuffer[State[T]]]()
  val objects = new ListBuffer[T]
  var count = 0

  def addNext(m: ValueMatcher, s: State[T]) = {
    next.get(m).map { _.append(s) }
      .getOrElse {
        val followers = new ListBuffer[State[T]]
        followers.append(s)
        next.put(m, followers)
      }
    count += 1
  }

  def getNext(): Iterable[(ValueMatcher, ListBuffer[State[T]])]

  def addTerminal(o: T) = objects += o
  def removeTerminal(o: T) = objects -= o
  def clearTerminals() = objects.clear
  def isTerminal() = !objects.isEmpty
  def getResult(): Option[T] = if(isTerminal()) Option(objects(0)) else None
  def getCount() = count

  def run(v: String) = {
    val matchingFollowers = next.find(_._1.matches(v)).map(_._2)
    matchingFollowers.getOrElse(new ListBuffer[State[T]]())
  }

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case null => false
      case s: State[T] => name == s.name
      case _ => false
    }
  }
}
