package com.ideal.eve.controller.auth

import scala.util.{Failure, Success, Try}

/**
 * Created by Christophe on 31/01/2016.
 */
object EveAuth {
  val EmptyAuth = new EveAuth {
    override def login(username: String, password: String): Try[String] = (username, password) match {
      case ("chris", "chris") => Success("chris")
      case _ => Failure(new Exception(s"Failed to authenticate user <$username>"))
    }
  }
  def login(login: String, password: String)(implicit authMethod: EveAuth = EmptyAuth): Try[String] = authMethod.login(login, password)
}

trait EveAuth {
  def login(username: String, password: String): Try[String]
}
