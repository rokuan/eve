package com.ideal.eve.universe.route

/**
  * Created by Christophe on 17/04/2016.
  */
trait Automaton[BranchType, T] {
  def add(o: T)
  def remove(o: T)
  def find(o: BranchType): Option[T]
}
