package com.ideal.eve

import com.ideal.eve.config.ServerParams
import com.ideal.eve.server.EveServer


/**
  * Created by Christophe on 12/03/2017.
  */
object Eve {
  def main(args: Array[String]): Unit = {
    val userServer = new EveServer(ServerParams.UserServerPort)
    userServer.start
  }
}
