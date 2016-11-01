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
    o.o.map { case (k, v) => (k -> eveObjectToMongoDBObject(v)) }.asDBObject : MongoDBObject

  implicit def eveObjectToMongoDBObject(o: EveObject): AnyRef = {
    o match {
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
      case EveStructuredObject(o) => null // TODO: transform the mapping into a MongoDBObject
      case EveObjectList(a) => MongoDBList(a.map(eveObjectToMongoDBObject(_)))
    }
  }
}

object EveObjectConversions {
  import EveObject._

  implicit def mongoDBObjectToEveObject(o: Any): EveObject = Try(EveObject(o)).getOrElse {
    o match {
      case m: MongoDBObject => m.map { case (k, v) => (k -> mongoDBObjectToEveObject(v)) }.toMap
      case l: MongoDBList => l.map(mongoDBObjectToEveObject).toArray
    }
  }
}