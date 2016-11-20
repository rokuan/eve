package com.ideal.eve.universe

import java.net.ServerSocket

import com.ideal.eve.config.PropertyManager
import com.ideal.evecore.universe.World

/**
  * Created by Christophe on 29/03/2016.
  */
class ReceiverServer extends Thread {
  def startServer(): Unit = {

  }

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

  }
}

object ReceiverServer {
  val Port = classOf[ReceiverServer] + ".port"
}
