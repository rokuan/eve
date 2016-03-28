package com.ideal.eve.universe

import com.mongodb.DBObject
import com.rokuan.calliopecore.sentence.IAction.ActionType

/**
  * Created by Christophe on 24/12/2015.
  */
object World {
  private val receivers = collection.mutable.Map[String, EveReceiver]()

  def registerReceiver(name: String, receiver: EveReceiver) = {
    receivers.put(name, receiver)
    receiver.initReceiver()
  }

  def unregisterReceiver(name: String) = receivers.remove(name).map(_.destroyReceiver())

  def unregisterAll(): Unit = receivers.keys.foreach(unregisterReceiver(_))

  def findReceiver(o: DBObject) = receivers.values.find(_.canHandle(o))
}
