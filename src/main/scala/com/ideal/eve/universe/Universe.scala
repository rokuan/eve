package com.ideal.eve.universe

import com.ideal.evecore.interpreter.EveStructuredObject
import com.ideal.evecore.universe.{MinimalWorld, World}
import com.ideal.evecore.universe.receiver.Receiver

/**
 * Created by chris on 01/03/17.
 */
object Universe extends World {
  private val world = new MinimalWorld

  override def registerReceiver(receiver: Receiver): Unit = world.registerReceiver(receiver)

  override def unregisterReceiver(receiver: Receiver): Unit = world.unregisterReceiver(receiver)

  override def findReceiver(o: EveStructuredObject): Option[Receiver] = world.findReceiver(o)
}
