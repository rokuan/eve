package com.ideal.eve.universe.route

import com.ideal.eve.universe.ValueMatcher

import scala.collection.mutable.ListBuffer
import scala.util.Try

/**
  * Created by Christophe on 17/04/2016.
  */
class State[T](val name: String) {
  val next = collection.mutable.Map[ValueMatcher, collection.mutable.Set[State[T]]]()
  val objects = new ListBuffer[T]

  def addNext(m: ValueMatcher, s: State[T]) = {
    next.getOrElseUpdate(m, collection.mutable.Set[State[T]]()).add(s)
  }

  def removeNext(m: ValueMatcher, s: State[T]) = {
    next.get(m).map { states =>
      states -= s
      if(states.isEmpty) next.remove(m)
    }
  }

  def getNext(): Iterable[(ValueMatcher, collection.mutable.Set[State[T]])] = next
  def getTerminalNext(m: ValueMatcher): Option[State[T]] = Try(next.get(m).get.head).toOption

  def addTerminal(o: T) = objects += o
  def removeTerminal(o: T) = objects -= o
  def clearTerminals() = objects.clear
  def isTerminal() = !objects.isEmpty
  def getResult(): Option[T] = if(isTerminal()) Option(objects(0)) else None
  def getCount() = next.size
  def isEmpty() = next.isEmpty

  override def equals(obj: scala.Any): Boolean = {
    obj match {
      case null => false
      case s: State[T] => name == s.name
      case _ => false
    }
  }
}

object State {
  val TerminalStateName = "_"
  val FinalStateName = "*"
}
