package com.ideal.eve.interpret

import java.text.{DateFormat, SimpleDateFormat}
import java.util.Date

import com.mongodb.casbah.query.Imports._
import com.google.gson.Gson
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.mongodb.util.JSON
import com.rokuan.calliopecore.json.FullGsonBuilder
import com.rokuan.calliopecore.sentence.structure.content.{ITimeObject, IPlaceObject}

/**
 * Created by Christophe on 27/09/2015.
 */

object EveObject {
  val NumberResultType = classOf[EveNumberObject]
  val StringResultType = classOf[EveStringObject]
  val BooleanResultType = classOf[EveBooleanObject]
  val ObjectResultType = classOf[EveStructuredObject]
  val DateResultType = classOf[EveTimeObject]
  val PlaceResultType = classOf[EvePlaceObject]

  def apply(value: Any) = convert(value)

  def convert(value: Any): EveObject = {
    value match {
      case b: java.lang.Boolean => new EveBooleanObject(b)
      case n: java.lang.Number => new EveNumberObject(n)
      case s: String => new EveStringObject(s)
      case d: Date => new EveDateObject(d)
      case t: ITimeObject => new EveTimeObject(t)
      case p: IPlaceObject => new EvePlaceObject(p)
      case o: MongoDBObject => new EveStructuredObject(o)
      case l: MongoDBList => new EveStructuredObjectList(l.map(EveObject(_)))
    }
  }
}

trait EveObject {
  def normalize(): AnyRef
}

case class EveBooleanObject(b: java.lang.Boolean) extends EveObject {
  override def normalize() = b
  override def toString() = b.toString
}
case class EveNumberObject(n: java.lang.Number) extends EveObject {
  override def normalize() = n
  override def toString() = n.toString
}
case class EveStringObject(s: String) extends EveObject {
  override def normalize() = s
  override def toString() = s
}
case class EveDateObject(d: Date) extends EveObject {
  override def normalize() = d
  override def toString() = DateFormat.getDateInstance(DateFormat.MEDIUM).format(d)
}
case class EveTimeObject(t: ITimeObject) extends EveObject {
  override def normalize() = {
    val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
    JSON.parse(gson.toJson(t, classOf[ITimeObject])).asInstanceOf[MongoDBObject]
  }
}
case class EvePlaceObject(p: IPlaceObject) extends EveObject {
  override def normalize() = {
    val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
    JSON.parse(gson.toJson(p, classOf[IPlaceObject])).asInstanceOf[MongoDBObject]
  }
}
case class EveStructuredObject(o: MongoDBObject) extends EveObject {
  override def normalize() = o
}

case class EveStructuredObjectList(a: Seq[EveObject]) extends EveObject {
  override def normalize() = a.map(_.normalize())
  override def toString() = a.mkString(", ")
}
