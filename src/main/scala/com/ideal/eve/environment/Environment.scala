package com.ideal.eve.environment

import com.ideal.eve.interpret.EveContext

import scala.collection.mutable

/**
  * Created by Christophe on 28/12/2016.
  */
class Environment {
  private val contexts: mutable.Stack[EveContext] = new mutable.Stack[EveContext]()
  contexts.push(new EveContext())
}
