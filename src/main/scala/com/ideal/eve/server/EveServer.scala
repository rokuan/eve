package com.ideal.eve.server

import java.net.Socket

import com.ideal.eve.controller.auth.EveAuth
import com.ideal.eve.db.{WordDatabase, EveEvaluator}
import com.ideal.eve.environment.EveEnvironment
import com.ideal.eve.universe.EveUniverse
import com.ideal.evecore.interpreter.{Evaluator, Environment}
import com.ideal.evecore.io.{UserServer, UserSocket}
import com.ideal.evecore.universe.World
import com.ideal.evecore.util.Result
import com.rokuan.calliopecore.fr.autoroute.parser.SentenceParser

import com.ideal.evecore.common.Conversions._

import scala.util.Try


/**
  * Created by Christophe on 31/01/2016.
  */
object EveServer {
  val ServerConfigurationFile = "server.properties"
}

class EveServer(port: Int) extends UserServer[EveSession](port) {
  def authenticate(login: String, password: String): Result[EveSession] = EveAuth.login(login, password).map(l => new EveSession(l))
  def connectUser(socket: Socket, user: EveSession): UserSocket[EveSession] = EveUser(socket, user)
}

object EveUser {
  def apply(socket: Socket, session: EveSession) = {
    val environment = new EveEnvironment
    val world = new EveUniverse
    val evaluator = new EveEvaluator(environment, world)(session)
    val parser = new SentenceParser(WordDatabase)
    new EveUser(socket, session, environment, world, evaluator, parser)
  }
}

class EveUser private (socket: Socket, session: EveSession, environment: Environment, world: World, evaluator: Evaluator, parser: SentenceParser) extends UserSocket[EveSession](socket, session, parser, evaluator, environment, world)