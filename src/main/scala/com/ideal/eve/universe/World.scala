package com.ideal.eve.universe

import com.ideal.eve.universe.route.implementation.ReceiverAutomaton
import com.mongodb.DBObject

/**
  * Created by Christophe on 24/12/2015.
  */
object World {
  private val receivers = collection.mutable.Map[String, EveReceiver]()
  private val automaton = new ReceiverAutomaton

  def registerReceiver(name: String, receiver: EveReceiver) = {
    receivers.put(name, receiver)
    automaton.add(receiver)
    receiver.initReceiver()
  }

  def unregisterReceiver(name: String) = {
    receivers.remove(name).map { receiver =>
      automaton.remove(receiver)
      receiver.destroyReceiver()
    }
  }

  def unregisterAll(): Unit = receivers.keys.foreach(unregisterReceiver(_))

  def findReceiver(o: ObjectValueSource) = automaton.find(o)
}
