package com.ideal.eve.server

import com.ideal.evecore.users.Session

/**
  * Created by Christophe on 13/03/2016.
  */
class EveSession(val username: String) extends Session {
  override def getUserName(): String = username
}