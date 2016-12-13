package com.ideal.eve

import com.ideal.eve.db.WordDatabase
import com.ideal.eve.server.EveServer

/**
 * Created by Christophe on 27/09/2015.
 */
object Main {
  def main(args: Array[String]) = {
    WordDatabase.init()
    val server = EveServer()
    server.start()
  }
}
