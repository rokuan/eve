package com.ideal.eve.server

import java.net.{ServerSocket, Socket}
import java.util.concurrent.atomic.AtomicBoolean

import com.ideal.eve.controller.auth.EveAuth
import com.ideal.eve.db.{EveEvaluator, WordDatabase}
import com.ideal.evecore.interpreter.remote.StreamUtils
import com.rokuan.calliopecore.fr.autoroute.parser.SentenceParser

import scala.util.control.Breaks
import scala.util.{Failure, Success}


/**
  * Created by Christophe on 31/01/2016.
  */
object EveServer {
  val ServerConfigurationFile = "server.properties"
}

class EveServer(val port: Int) extends Thread with AutoCloseable {
  val server = new ServerSocket(port)
  val users = collection.mutable.Map[String, EveUser]()

  override def run() = {
    val breaks = new Breaks

    breaks.breakable {
      while(true){
        try {
          val client = server.accept()
          val is = client.getInputStream
          val loginData = new Array[Byte](is.read() & 0xFF)
          is.read(loginData)
          val passwordData = new Array[Byte](is.read() & 0xFF)
          is.read(passwordData)

          val login = new String(loginData)
          val password = new String(passwordData)

          EveAuth.login(login, password) match {
            case Success(l) => {
              val user = new EveUser(client)(new EveSession(l))
              val os = client.getOutputStream
              os.write('Y')
              os.flush()
              users += (login -> user)
              user.start()
            }
            case Failure(e) => {
              val os = client.getOutputStream
              os.write('N')
              os.flush()
              client.close()
            }
          }
        } catch {
          case t: Throwable => breaks.break()
        }
      }
    }
  }

  override def close(): Unit = server.close()
}

class EveUser(val socket: Socket)(implicit val session: EveSession) extends Thread with StreamUtils {
  val evaluator = new EveEvaluator()(session)
  val parser = new SentenceParser(new WordDatabase)
  val connected = new AtomicBoolean(false)

  override def run(): Unit = {
    while(connected.get()){
      try {
        /*val dataLength = new Array[Byte](4)

        if(is.read(dataLength, 0, dataLength.length) != dataLength.length){
          connected = false
        } else {
          var dataSize = (0 until dataLength.length).map(i => (dataLength(i) & 0xFF) << (8 * i)).foldLeft(0)(_ + _)
          val text = new StringBuilder

          while(dataSize > 0){
            val tmp = new Array[Byte](1024)
            val size = is.read(tmp, 0, tmp.length)

            if(size > 0){
              text.append(new String(tmp, 0, size))
              dataSize -= size
            } else {
              // TODO: error
            }
          }

          val obj = parser.parseText(text.toString())*/
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
}