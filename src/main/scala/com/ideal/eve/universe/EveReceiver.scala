package com.ideal.eve.universe

/**
  * Created by Christophe on 24/12/2015.
  */
trait EveReceiver {
  def handleMessage(message: Message)
}
