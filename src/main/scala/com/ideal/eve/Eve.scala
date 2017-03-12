package com.ideal.eve

import com.ideal.eve.config.ServerParams
import com.ideal.eve.environment.EveEnvironment
import com.ideal.eve.server.EveServer
import com.ideal.eve.universe.EveUniverse
import com.ideal.evecore.io.{ContextServer, ReceiverServer}


/**
  * Created by Christophe on 12/03/2017.
  */
object Eve {
  protected val environment = EveEnvironment
  protected val world = EveUniverse
  protected val userServer = new EveServer(ServerParams.UserServerPort)
  protected val contextServer = new ContextServer(environment, ServerParams.ContextServerPort)
  protected val receiverServer = new ReceiverServer(world, ServerParams.ReceiverServerPort)

  def main(args: Array[String]): Unit = {
    userServer.start
    contextServer.start()
    receiverServer.start()
  }
}
