package com.ideal.eve.interpret

import com.ideal.evecore.interpreter.{EveObject, History}
import com.mongodb.casbah.MongoCollection

/**
 * Created by chris on 01/03/17.
 */
class EveHistory(val historyCollection: MongoCollection) extends History {  // TODO: add the session to get the history for a specific user
  override def addItem(o: EveObject): Unit = ???

  override def getLastItem(): Option[EveObject] = ???

  override def getLastItemOfType(t: String): Option[EveObject] = ???

  override def addPlace(o: EveObject): Unit = ???

  override def getLastPlace(): Option[EveObject] = ???
}
