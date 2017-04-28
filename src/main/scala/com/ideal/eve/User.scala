package com.ideal.eve

import com.ideal.eve.config.ServerParams
import com.ideal.evecore.common.Credentials
import com.ideal.evecore.io.UserConnection

/**
 * Created by chris on 05/04/2017.
 */
object User {
  def main(args: Array[String]): Unit = {
    val connection = new UserConnection("localhost", ServerParams.UserServerPort, new Credentials("chris", "chris"))
    connection.start()
    println(connection.evaluate("quelle est la location"))
    //connection.disconnect()
  }
}
