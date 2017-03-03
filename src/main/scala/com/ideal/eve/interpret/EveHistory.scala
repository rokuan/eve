package com.ideal.eve.interpret

import com.ideal.evecore.interpreter.{EveObject, History}
import com.mongodb.casbah.MongoCollection

/**
 * Created by chris on 01/03/17.
 */
class EveHistory(val historyCollection: MongoCollection) extends History {
  override def addItem(o: EveObject): Unit = ???

  override def getLastItem(): Option[EveObject] = ???

  override def getLastItemOfType(t: String): Option[EveObject] = ???

  override def addPlace(o: EveObject): Unit = ???

  override def getLastPlace(): Option[EveObject] = ???
}
