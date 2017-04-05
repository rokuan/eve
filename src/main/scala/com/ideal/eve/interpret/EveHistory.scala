package com.ideal.eve.interpret

import com.ideal.evecore.interpreter.{EveStructuredObject, EveObject, History}
import com.mongodb.casbah.MongoCollection

/**
 * Created by chris on 01/03/17.
 */
// TODO: complete the methods
class EveHistory(val historyCollection: MongoCollection) extends History {  // TODO: add the session to get the history for a specific user
  override def getLastItem(): Option[EveStructuredObject] = Option.empty[EveStructuredObject]

  override def getLastItemOfType(t: String): Option[EveStructuredObject] = Option.empty[EveStructuredObject]

  override def getLastPlace(): Option[EveStructuredObject] = Option.empty[EveStructuredObject]

  override def addItem(o: EveStructuredObject): Unit = {}

  override def addPlace(o: EveStructuredObject): Unit = {}
}
