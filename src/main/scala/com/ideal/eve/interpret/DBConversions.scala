package com.ideal.eve.interpret

import com.google.gson.Gson
import com.ideal.evecore.interpreter._
import com.ideal.evecore.interpreter.data.EveMappingObject
import com.mongodb.casbah.commons.{MongoDBList, MongoDBObject}
import com.mongodb.util.JSON
import com.rokuan.calliopecore.json.FullGsonBuilder
import com.rokuan.calliopecore.sentence.structure.content.{IPlaceObject, ITimeObject}

import scala.collection.JavaConversions._

import scala.util.Try

import com.mongodb.casbah.query.Imports._

/**
 * Created by Christophe on 27/09/2015.
 */

object EveObjectConverters {
  /*implicit def eveStructuredObjectToMongoDBObject(o: EveStructuredObject): MongoDBObject =
    o.o.map { case (k, v) => (k -> eveObjectToMongoDBObject(v)) }.asDBObject*/
  implicit def eveMappingObjectToMongoDBObject(o: EveMappingObject): MongoDBObject =
    o.getValues.map { case (k, v) => (k -> eveObjectToMongoDBObject(v)) }.asDBObject

  implicit def eveObjectToMongoDBObject(o: EObject): AnyRef = {
    o match {
      case null => null
      case EBooleanObject(b) => b
      case ENumberObject(n) => n
      case EStringObject(s) => s
      case EDateObject(d) => d
      case ETimeObject(t) => {
        val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
        JSON.parse(gson.toJson(t, classOf[ITimeObject])).asInstanceOf[MongoDBObject]
      }
      case EPlaceObject(p) => {
        val gson: Gson = FullGsonBuilder.getSerializationGsonBuilder.create()
        JSON.parse(gson.toJson(p, classOf[IPlaceObject])).asInstanceOf[MongoDBObject]
      }
      //case o: EveStructuredObject => eveStructuredObjectToMongoDBObject(o)
      case o: EveMappingObject => eveMappingObjectToMongoDBObject(o)
      case EObjectList(a) => MongoDBList(a.map(eveObjectToMongoDBObject(_)))
      case EveObjectId(id) => id
      case _ => MongoDBObject()
    }
  }
}

object EveObjectConversions {
  /*implicit def dbObjectToEveObject(o: Any): EveObject = Try(EveObject(o)).getOrElse {
    o match {
      case o: BasicDBObject =>
        val mongoObject: MongoDBObject = o
        mongoObject
      case l: BasicDBList =>
        val mongoList: MongoDBList = l
        mongoList
      case id: ObjectId => EveObjectId(id)
    }
  }*/

  implicit def dbObjectToEveObject(o: Any): EObject = Try(implicitly[EObject](o)).getOrElse {
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

  implicit def dbDataToEveObject(o: AnyRef): EObject = o: Any

  implicit def mongoDBObjectToEveObject(o: MongoDBObject): EObject =
    o.map { case (k, v) => (k -> dbObjectToEveObject(v)) }.toMap

  implicit def mongoDBListToEveObject(o: MongoDBList): EObject =
    EObjectList(o.map(dbObjectToEveObject))
}

case class EveObjectId(o: ObjectId) extends EObject