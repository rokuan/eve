package com.ideal.eve.environment

import com.ideal.eve.interpret.EveDatabaseContext
import com.ideal.evecore.interpreter.{EveObjectList, EveObject, Context}

import scala.collection.mutable

/**
  * Created by Christophe on 28/12/2016.
  */
object Environment extends Context {
  private val contexts: mutable.ListBuffer[Context] = new mutable.ListBuffer[Context]()
  contexts += (EveDatabaseContext())

  override def findItemsOfType(t: String): Option[EveObject] = {
    val results = contexts.map(_.findItemsOfType(t)).flatten
    val flattenedResults = results.foldLeft(List[EveObject]()){
      case (acc, o: EveObjectList) => o.a.toList ++ acc
      case (acc, o) => o :: acc
    }
    if(flattenedResults.size == 1){
      Some(flattenedResults.head)
    } else {
      Some(EveObjectList(flattenedResults))
    }
  }

  def addContext(context: Context) = contexts += context
  def removeContext(context: Context) = contexts -= context
}


/*class Environment extends Context {
  private val contexts: mutable.Stack[Context] = new mutable.Stack[Context]()
  //contexts.push(new EveContext())

  override def findItemsOfType(t: String): Option[EveObject] = {
    val results = contexts.map(_.findItemsOfType(t)).flatten
    val flattenedResults = results.foldLeft(List[EveObject]()){
      case (acc, o: EveObjectList) => o.a.toList ++ acc
      case (acc, o) => o :: acc
    }
    if(flattenedResults.size == 1){
      Some(flattenedResults.head)
    } else {
      Some(EveObjectList(flattenedResults))
    }
  }
}*/
