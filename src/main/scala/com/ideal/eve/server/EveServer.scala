package com.ideal.eve.server

import java.net.Socket

import com.ideal.eve.controller.auth.EveAuth
import com.ideal.eve.db.{WordDatabase, EveDatabase, EveEvaluator}
import com.ideal.eve.environment.EveEnvironment
import com.ideal.eve.universe.EveUniverse
import com.ideal.evecore.interpreter.{Environment, Evaluator}
import com.ideal.evecore.io.{UserSocket, UserServer}
import com.ideal.evecore.universe.World
import com.rokuan.calliopecore.fr.autoroute.parser.SentenceParser
import com.rokuan.calliopecore.parser.AbstractParser

import scala.util.Try


/**
  * Created by Christophe on 31/01/2016.
  */
object EveServer {
  val ServerConfigurationFile = "server.properties"
}

class EveServer(port: Int) extends UserServer[EveSession](port) {
  override def authenticate(login: String, password: String): Try[EveSession] = EveAuth.login(login, password).map(l => new EveSession(l))
  override def connectUser(socket: Socket, user: EveSession): UserSocket[EveSession] = new EveUser(socket, user)
}

class EveUser(socket: Socket, session: EveSession) extends UserSocket[EveSession](socket, session) {
  override protected val environment: Environment = new EveEnvironment()
  override protected val world: World = new EveUniverse()
  override protected val evaluator: Evaluator = new EveEvaluator(environment, world)(session)
  override protected val parser: AbstractParser = new SentenceParser(WordDatabase)
}

/*class EveUser(val socket: Socket)(implicit val session: EveSession) extends Thread with StreamUtils {
  val evaluator = new EveEvaluator()(session)
  val parser = new SentenceParser(new WordDatabase)
  val connected = new AtomicBoolean(true)

  override def run(): Unit = {
    while(connected.get()){
      try {
        val text = readValue()
        Option(text).map { t =>
          val obj = parser.parseText(t)
          println(evaluator.eval(obj))
        }.getOrElse(connected.set(false))
      } catch {
        case e: Throwable => {
          e.printStackTrace()
          connected.set(false)
        }
      }
    }

    socket.close()
  }
}*/