package com.ideal.eve.interpret

import com.google.gson.Gson
import com.ideal.evecore.interpreter._
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.mongodb.util.JSON
import com.rokuan.calliopecore.json.FullGsonBuilder
import com.rokuan.calliopecore.sentence.structure.content.{IPlaceObject, ITimeObject}

import scala.util.Try

import com.mongodb.casbah.query.Imports._

/**
 * Created by Christophe on 27/09/2015.
 */

object EveObjectConverters {
  implicit def eveStructuredObjectToMongoDBObject(o: EveStructuredObject): MongoDBObject =
    o.o.map { case (k, v) => (k -> eveObjectToMongoDBObject(v)) }.asDBObject

  implicit def eveObjectToMongoDBObject(o: EveObject): AnyRef = {
    o match {
      case null => null
      case EveBooleanObject(b) => b
      case EveNumberObject(n) => n
      case EveStringObject(s) => s
      case EveDateObject(d) => d
      case EveTimeObject(t) => {
        val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
        JSON.parse(gson.toJson(t, classOf[ITimeObject])).asInstanceOf[MongoDBObject]
      }
      case EvePlaceObject(p) => {
        val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
        JSON.parse(gson.toJson(p, classOf[IPlaceObject])).asInstanceOf[MongoDBObject]
      }
      case o: EveStructuredObject => eveStructuredObjectToMongoDBObject(o)
      case EveObjectList(a) => MongoDBList(a.map(eveObjectToMongoDBObject(_)))
      case EveObjectId(id) => id
    }
  }
}

object EveObjectConversions {
  implicit def dbObjectToEveObject(o: Any): EveObject = Try(EveObject(o)).getOrElse {
    o match {
      case o: BasicDBObject =>
        val mongoObject: MongoDBObject = o
        mongoObject
      case l: BasicDBList =>
        val mongoList: MongoDBList = l
        mongoList
      case id: ObjectId => EveObjectId(id)
    }
  }

  implicit def mongoDBObjectToEveObject(o: MongoDBObject): EveObject =
    EveStructuredObject(o.map { case (k, v) => (k -> dbObjectToEveObject(v)) }.toMap)

  implicit def mongoDBListToEveObject(o: MongoDBList): EveObject =
    EveObjectList(o.map(dbObjectToEveObject))
}

case class EveObjectId(o: ObjectId) extends EveObject