package com.ideal.eve

import java.io.File
import java.util.UUID

import com.ideal.eve.config.ServerParams
import com.ideal.evecore.common.{Credentials, Mapping}
import com.ideal.evecore.interpreter.{Context, EObject, QuerySource}
import com.ideal.evecore.interpreter.EObject._
import com.ideal.evecore.interpreter.data._
import com.ideal.evecore.io.UserConnection
import com.ideal.evecore.universe.matcher.{ValueMatcher, ValueMatcherUtils}
import com.ideal.evecore.universe.receiver.{EveObjectMessage, Receiver}
import com.ideal.evecore.util.{Result, Option => EOpt}
import com.ideal.evecore.common.Conversions._
import com.ideal.evecore.interpreter.EveObjectDSL._
import com.ideal.evecore.util.{Pair => EPair}

/**
 * Created by chris on 05/04/2017.
 */
object User {
  class TestReceiver extends Receiver {
    protected val mappings = ValueMatcherUtils.parseJson(new File("map_receiver.json"))

    override def getReceiverName: String = getClass.getName

    override def getMappings: Mapping[ValueMatcher] = mappings

    override def handleMessage(message: EveObjectMessage): Result[EveObject] = {
      val content = implicitly[EObject](message.getContent)
      val location = (content \ "what")
      val objectType = location.getType()
      if (objectType == "location") {
        val latitude = (location \ "latitude").toNumber
        val longitude = (location \ "longitude").toNumber
        println("lat=" + latitude.doubleValue() + ",lng=" + longitude.doubleValue())
        EveResultObject.ok(Map[String, EveObject](
          "latitude" -> latitude.doubleValue(),
          "longitude" -> longitude.doubleValue()
        ))
      } else {
        EveResultObject.ok()
      }
    }

    override def initReceiver(): Unit = {}

    override def destroyReceiver(): Unit = {}
  }

  class TestContext extends Context with QuerySource {
    protected val locationId = UUID.randomUUID().toString

    protected def getLocation(): EveStructuredObject = {
      new EveQueryMappingObject(
        locationId,
        new EPair[String, EveObject](EveObject.TYPE_KEY, "location"),
        new EPair[String, EveObject]("latitude", 48.7),
        new EPair[String, EveObject]("longitude", 2.07)
      )
    }

    override def findOneItemOfType(`type`: String): EOpt[EveStructuredObject] = Option(`type`).collect {
      case "LOCATION" => getLocation()
    }

    override def findItemsOfType(`type`: String): EOpt[EveObjectList] = EOpt.empty[EveObjectList]()

    override def findById(id: String): EOpt[EveStructuredObject] = if (id == locationId) { Option.apply(getLocation()) } else { Option.empty[EveStructuredObject] }
  }

  def main(args: Array[String]): Unit = {
    val context = new TestContext
    val receiver = new TestReceiver
    val connection = new UserConnection("localhost", ServerParams.UserServerPort, new Credentials("chris", "chris"))
    connection.start()
    connection.registerContext(context)
    connection.registerReceiver(receiver)
    try {
      val result = connection.evaluate("affiche la position")
      if (result.isSuccess)
          println(result.get())
        else
          result.getError.printStackTrace()
    } catch {
      case t: Throwable => t.printStackTrace()
    }
    connection.unregisterContext(context)
    connection.unregisterReceiver(receiver)
    connection.disconnect()
  }
}
