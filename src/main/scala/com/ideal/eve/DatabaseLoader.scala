package com.ideal.eve

import com.ideal.eve.db.WordDatabase

/**
  * Created by Christophe on 14/12/2016.
  */
object DatabaseLoader {
  def main(args: Array[String]): Unit = WordDatabase.init()
}
