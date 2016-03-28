package com.ideal.eve.universe

import java.net.ServerSocket

import com.ideal.eve.config.PropertyManager

/**
  * Created by Christophe on 29/03/2016.
  */
class ReceiverServer extends Thread {
  override def run(): Unit = {
    val server = new ServerSocket(PropertyManager.get(ReceiverServer.Port, 7981))
    var connected = true

    try {
      while(connected){
        val receiverSocket = server.accept()
        // TODO:
      }
    } catch {
      case _: Throwable => connected = false
    }
  }

  def stopServer(): Unit = {
    World.unregisterAll()
  }
}

object ReceiverServer {
  val Port = classOf[ReceiverServer] + ".port"
}
