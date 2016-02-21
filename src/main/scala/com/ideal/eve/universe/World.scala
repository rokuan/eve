package com.ideal.eve.universe

import com.rokuan.calliopecore.sentence.IAction.ActionType

/**
  * Created by Christophe on 24/12/2015.
  */
object World {
  private val receivers = collection.mutable.Map[String, EveReceiver]()

  def registerReceiver(name: String, receiver: EveReceiver) = receivers.put(name, receiver)
  def unregisterReceiver(name: String) = receivers.remove(name)
  def getReceiver(name: String): Option[EveReceiver] = receivers.get(name)

  def execute(action: ActionType, args: Any*) = {

  }
}
