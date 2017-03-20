package com.ideal.eve.config

/**
  * Created by Christophe on 12/03/2017.
  */
object ServerParams {
  val ServerParamsClass = classOf[ServerParams].getName
  val UserServerPortParameter = ServerParamsClass + "userServer.port"
  val ContextServerPortParameter = ServerParamsClass + ".contextSever.port"
  val ReceiverServerPortParameter = ServerParamsClass + ".receiverServer.port"
  val DefaultUserServerPort = 7981
  val DefaultContextServerPort = 7982
  val DefaultReceiverServerPort = 7983

  lazy val UserServerPort = PropertyManager.get(UserServerPortParameter, DefaultUserServerPort)
  lazy val ContextServerPort = PropertyManager.get(ContextServerPortParameter, DefaultContextServerPort)
  lazy val ReceiverServerPort = PropertyManager.get(ReceiverServerPortParameter, DefaultReceiverServerPort)
}

class ServerParams