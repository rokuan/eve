package com.ideal.eve.server

import java.net.{Socket, ServerSocket}
import java.util.Properties

import akka.actor.{ActorSystem, Props, Actor}
import akka.actor.Actor.Receive
import com.ideal.eve.config.PropertyManager
import com.ideal.eve.controller.EveAuth
import com.ideal.eve.interpret.Evaluator
import com.rokuan.calliopecore.sentence.structure.InterpretationObject

import scala.collection.mutable.ListBuffer
import scala.util.{Failure, Success}

sealed trait EveServerMessage
case object StartServerMessage extends EveServerMessage
case object StopServerMessage extends EveServerMessage

/**
  * Created by Christophe on 31/01/2016.
  */
object EveServer {
  val context = ActorSystem("EveServer")

  val ServerConfigurationFile = "server.properties"
  val HostProperty = classOf[EveServer].getName + ".host"
  val PortProperty = classOf[EveServer].getName + ".port"

  def apply() = {
    new EveServer(PropertyManager.get(HostProperty, "localhost"),
      PropertyManager.get(PortProperty, 7980))
  }
}

class EveServer(val host: String, val port: Int) extends AutoCloseable {
  var actor = EveServer.context.actorOf(Props(new EveServerActor(port)))

  def start() = {
    actor ! StartServerMessage
  }

  def stop() = {
    actor ! StopServerMessage
  }

  override def close(): Unit = stop
}

class EveServerActor(val port: Int) extends Actor {
  var server: ServerSocket = null
  var running: Boolean = false
  val users: collection.mutable.Map[String, EveUser] = collection.mutable.Map[String, EveUser]()

  override def receive = {
    case StartServerMessage => {
      server = new ServerSocket(port)
      running = true

      while(running){
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
              val user = new EveUser(client)
              val os = client.getOutputStream
              os.write('Y')
              os.flush()
              users += (login -> user)
              new Thread(user).start()
            }
            case Failure(e) => {
              val os = client.getOutputStream
              os.write('N')
              os.flush()
              client.close()
            }
          }
        } catch {
          case t: Throwable =>
        }
      }
    }

    case StopServerMessage => {
      if(server != null){
        server.close()
      }
      running = false
    }
  }
}

class EveUser(val socket: Socket) extends Runnable {
  override def run(): Unit = {
    var connected = true
    val is = socket.getInputStream

    while(connected){
      try {
        val dataLength = new Array[Byte](4)

        if(is.read(dataLength, 0, dataLength.length) != dataLength.length){
          connected = false
        } else {
          var dataSize = (0 until dataLength.length).map(i => (dataLength(i) & 0xFF) << (8 * i)).foldLeft(0)(_ + _)
          val json = new StringBuilder

          while(dataSize > 0){
            val tmp = new Array[Byte](1024)
            val size = is.read(tmp, 0, tmp.length)

            if(size > 0){
              json.append(new String(tmp, 0, size))
              dataSize -= size
            } else {
              // TODO: error
            }
          }

          println(json.toString())
          val obj = InterpretationObject.fromJSON(json.toString)
          println(new Evaluator().eval(obj))
        }

      } catch {
        case _: Throwable => connected = false
      }
    }

    socket.close()
  }
}